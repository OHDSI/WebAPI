package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnBean(ShinyService.class)
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PositConnectClient implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(PositConnectClient.class);
    private static final MediaType JSON_TYPE = MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE);
    private static final MediaType OCTET_STREAM_TYPE = MediaType.parse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);
    private static final String HEADER_AUTH = "Authorization";
    private static final String AUTH_PREFIX = "Key";

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired(required = false)
    private PositConnectProperties properties;
    public UUID createContentItem(ApplicationBrief brief) {
        ContentItem contentItem = new ContentItem();
        contentItem.accessType = "acl";
        contentItem.name = brief.getName();
        contentItem.title = brief.getTitle();
        contentItem.description = brief.getDescription();
        RequestBody body = RequestBody.create(toJson(contentItem), JSON_TYPE);
        String url = connect("/v1/content");
        ContentItemResponse response = doPost(ContentItemResponse.class, url, body);
        return response.guid;
    }

    public List<ContentItemResponse> listContentItems() {
        String url = connect("/v1/content");
        Request.Builder request = new Request.Builder()
                .url(url);
        return doCall(new TypeReference<List<ContentItemResponse>>() {}, request, url);
    }

    public String uploadBundle(UUID contentId, TemporaryFile bundle) {
        String url = connect(MessageFormat.format("/v1/content/{0}/bundles", contentId));
        BundleResponse response = doPost(BundleResponse.class, url, RequestBody.create(bundle.getFile().toFile(), OCTET_STREAM_TYPE));
        return response.id;
    }

    public String deployBundle(UUID contentId, String bundleId) {
        String url = connect(MessageFormat.format("/v1/content/{0}/deploy", contentId));
        BundleRequest request = new BundleRequest();
        request.bundleId = bundleId;
        RequestBody requestBody = RequestBody.create(toJson(request), JSON_TYPE);
        BundleDeploymentResponse response = doPost(BundleDeploymentResponse.class, url, requestBody);
        return response.taskId;
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
        try(Response response = call.execute()) {
            if (!response.isSuccessful()) {
                log.error("Request [{}] returned code: [{}], message: [{}]", url, response.code(), response.message());
                String message = MessageFormat.format("Request [{0}] returned code: [{1}], message: [{2}]", url, response.code(), response.message());
                if (response.code() == 409) {
                    throw new ConflictPositConnectException(message);
                }
                throw new PositConnectClientException(message);
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
        }
    }

    private String connect(String path) {
        return StringUtils.removeEnd(properties.getUrl(), "/") + "/__api__/" + StringUtils.removeStart(path, "/");
    }

    public static class ContentItem {
        public String name;
        public String title;
        public String description;
        @JsonProperty("access_type")
        public String accessType;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentItemResponse extends ContentItem {
        public UUID guid;
        @JsonProperty("owner_guid")
        public UUID ownerGuid;
        public String id;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BundleResponse {
        public String id;
        @JsonProperty("content_guid")
        public String contentGuid;
        @JsonProperty("created_time")
        public Instant createdTime;
        @JsonProperty("cluster_name")
        public String clusterName;
        @JsonProperty("image_name")
        public String imageName;
        @JsonProperty("r_version")
        public String rVersion;
        @JsonProperty("r_environment_management")
        public Boolean rEnvironmentManagement;
        @JsonProperty("py_version")
        public String pyVersion;
        @JsonProperty("py_environment_management")
        public Boolean pyEnvironmentManagement;
        @JsonProperty("quarto_version")
        public String quartoVersion;
        public Boolean active;
        public Integer size;
    }

    static class BundleRequest {
        @JsonProperty("bundle_id")
        public String bundleId;
    }

    static class BundleDeploymentResponse {
        @JsonProperty("task_id")
        public String taskId;
    }
}
