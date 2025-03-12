package org.ohdsi.webapi.shiny.posit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.service.ShinyService;
import org.ohdsi.webapi.shiny.ApplicationBrief;
import org.ohdsi.webapi.shiny.ConflictPositConnectException;
import org.ohdsi.webapi.shiny.TemporaryFile;
import org.ohdsi.webapi.shiny.posit.dto.AddTagRequest;
import org.ohdsi.webapi.shiny.posit.dto.BundleDeploymentResponse;
import org.ohdsi.webapi.shiny.posit.dto.BundleRequest;
import org.ohdsi.webapi.shiny.posit.dto.BundleResponse;
import org.ohdsi.webapi.shiny.posit.dto.ContentItem;
import org.ohdsi.webapi.shiny.posit.dto.ContentItemResponse;
import org.ohdsi.webapi.shiny.posit.dto.TagMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnBean(ShinyService.class)
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PositConnectClient implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(PositConnectClient.class);
    private static final int BODY_BYTE_COUNT_TO_LOG = 10_000;
    private static final MediaType JSON_TYPE = MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE);
    private static final MediaType OCTET_STREAM_TYPE = MediaType.parse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);
    private static final String HEADER_AUTH = "Authorization";
    private static final String AUTH_PREFIX = "Key";

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired(required = false)
    private PositConnectProperties properties;

    public UUID createContentItem(ApplicationBrief brief) {
        ContentItem contentItem = new ContentItem();
        contentItem.setAccessType("acl");
        contentItem.setName(brief.getName());
        contentItem.setTitle(brief.getTitle());
        contentItem.setDescription(brief.getDescription());
        RequestBody body = RequestBody.create(toJson(contentItem), JSON_TYPE);
        String url = connect("/v1/content");
        ContentItemResponse response = doPost(ContentItemResponse.class, url, body);
        return response.getGuid();
    }

    public List<ContentItemResponse> listContentItems() {
        String url = connect("/v1/content");
        Request.Builder request = new Request.Builder()
                .url(url);
        return doCall(new TypeReference<List<ContentItemResponse>>() {
        }, request, url);
    }

    public List<TagMetadata> listTags() {
        String url = connect("/v1/tags");
        Request.Builder request = new Request.Builder()
                .url(url);
        return doCall(new TypeReference<List<TagMetadata>>() {
        }, request, url);
    }

    public void addTagToContent(UUID contentId, AddTagRequest addTagRequest) {
        String url = connect(MessageFormat.format("/v1/content/{0}/tags", contentId));
        RequestBody requestBody = RequestBody.create(toJson(addTagRequest), JSON_TYPE);
        doPost(Void.class, url, requestBody);
    }

    public String uploadBundle(UUID contentId, TemporaryFile bundle) {
        String url = connect(MessageFormat.format("/v1/content/{0}/bundles", contentId));
        BundleResponse response = doPost(BundleResponse.class, url, RequestBody.create(bundle.getFile().toFile(), OCTET_STREAM_TYPE));
        return response.getId();
    }

    public String deployBundle(UUID contentId, String bundleId) {
        String url = connect(MessageFormat.format("/v1/content/{0}/deploy", contentId));
        BundleRequest request = new BundleRequest();
        request.setBundleId(bundleId);
        RequestBody requestBody = RequestBody.create(toJson(request), JSON_TYPE);
        BundleDeploymentResponse response = doPost(BundleDeploymentResponse.class, url, requestBody);
        return response.getTaskId();
    }

    private <T> T doPost(Class<T> responseClass, String url, RequestBody requestBody) {
        Request.Builder request = new Request.Builder()
                .url(url)
                .post(requestBody);
        return doCall(responseClass, request, url);
    }

    private <T> T doCall(Class<T> responseClass, Request.Builder request, String url) {
        return doCall(new TypeReference<T>() {
            @Override
            public Type getType() {
                return responseClass;
            }
        }, request, url);
    }

    private <T> T doCall(TypeReference<T> responseClass, Request.Builder request, String url) {
        Call call = call(request, properties.getApiKey());
        try (Response response = call.execute()) {
            if (!response.isSuccessful()) {
                log.error("Request [{}] returned code: [{}], message: [{}], bodyPart: [{}]", url, response.code(), response.message(), response.body() != null ? response.peekBody(BODY_BYTE_COUNT_TO_LOG).string() : "");
                String message = MessageFormat.format("Request [{0}] returned code: [{1}], message: [{2}]", url, response.code(), response.message());
                if (response.code() == 409) {
                    throw new ConflictPositConnectException(message);
                }
                throw new PositConnectClientException(message);
            }
            if (responseClass.getType() == Void.class) {
                return null;
            }
            if (response.body() == null) {
                log.error("Failed to create a content, an empty result returned [{}]", url);
                throw new PositConnectClientException("Failed to create a content, an empty result returned");
            }
            return objectMapper.readValue(response.body().charStream(), responseClass);
        } catch (IOException e) {
            log.error("Failed to execute call [{}]", url, e);
            throw new PositConnectClientException(MessageFormat.format("Failed to execute call [{0}]: {1}", url, e.getMessage()));
        }
    }

    private <T> String toJson(T value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Failed to execute Connect request", e);
            throw new PositConnectClientException("Failed to execute Connect request", e);
        }
    }

    private Call call(Request.Builder request, String token) {
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectTimeout(properties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(properties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(properties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .build();
        return client.newCall(request.header(HEADER_AUTH, AUTH_PREFIX + " " + token).build());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (properties != null) {
            if (StringUtils.isBlank(properties.getApiKey())) {
                log.error("Set Posit Connect API Key to property \"shiny.connect.api.key\"");
                throw new BeanInitializationException("Set Posit Connect API Key to property \"shiny.connect.api.key\"");
            }
            if (StringUtils.isBlank(properties.getUrl())) {
                log.error("Set Posit Connect URL to property \"shiny.connect.url\"");
                throw new BeanInitializationException("Set Posit Connect URL to property \"shiny.connect.url\"");
            }
            if (Objects.isNull(properties.getTimeoutSeconds())) {
                log.error("Set Posit Connect HTTP Connect/Read/Write Timeout to property \"shiny.connect.okhttp.timeout.seconds\"");
                throw new BeanInitializationException("Set Posit Connect HTTP Connect/Read/Write Timeout to property \"shiny.connect.okhttp.timeout.seconds\"");
            }
        }
    }

    private String connect(String path) {
        return StringUtils.removeEnd(properties.getUrl(), "/") + "/__api__/" + StringUtils.removeStart(path, "/");
    }
}
