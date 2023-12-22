package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnBean(ShinyService.class)
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PositConnectClient implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(PositConnectClient.class);
    private static final MediaType JSON_TYPE = MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE);
    private static final String HEADER_AUTH = "Authorization";
    private static final String AUTH_PREFIX = "Key";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${shiny.connect.api.key}")
    private String apiKey;
    @Value("${shiny.connect.url}")
    private String connectUrl;

    public UUID createContentItem(ApplicationBrief brief) {
        ContentItem contentItem = new ContentItem();
        contentItem.accessType = "acl";
        contentItem.name = brief.getName();
        contentItem.title = brief.getTitle();
        contentItem.description = brief.getDescription();
        RequestBody body = RequestBody.create(toJson(contentItem), JSON_TYPE);
        String url = connect("/v1/content");
        Request.Builder request = new Request.Builder()
                .url(url)
                .post(body);
        Call call = call(request, apiKey);
        try(Response response = call.execute()) {
            log.debug("Call [{}] returned [{}]", url, response.code());
            if (response.body() == null) {
                log.error("Failed to create a content, an empty result returned [{}]", url);
                throw new PositConnectClientException("Failed to create a content, an empty result returned");
            }
            ContentItemResponse contentItemResponse = objectMapper.readValue(response.body().charStream(),
                    ContentItemResponse.class);
            return contentItemResponse.guid;
        } catch (IOException e) {
            log.error("Failed to execute call [{}]", url, e);
            throw new PositConnectClientException("Failed to execute call: " + e.getMessage());
        }
    }

    public Integer uploadBundle(UUID contentId, TemporaryFile bundle) {
        return null;
    }

    public String deployBundle(UUID contentId, Integer bundleId) {
        return null;
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
        if (StringUtils.isBlank(apiKey)) {
            log.error("Set Posit Connect API Key to property \"shiny.connect.api_key\"");
            throw new BeanInitializationException("Set Posit Connect API Key to property \"shiny.connect.api_key\"");
        }
        if (StringUtils.isBlank(connectUrl)) {
            log.error("Set Posit Connect URL to property \"shiny.connect.url\"");
            throw new BeanInitializationException("Set Posit Connect URL to property \"shiny.connect.url\"");
        }
    }

    private String connect(String path) {
        return StringUtils.removeEnd(connectUrl, "/") + "/" + StringUtils.removeStart(path, "/");
    }

    static class ContentItem {
        public String name;
        public String title;
        public String description;
        @JsonProperty("access_type")
        public String accessType;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ContentItemResponse extends ContentItem {
        public UUID guid;
        @JsonProperty("owner_guid")
        public UUID ownerGuid;
        public String id;
    }
}
