package org.ohdsi.webapi.shiny.posit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shiny.connect")
public class PositConnectProperties {
    @Value("${shiny.connect.api.key}")
    private String apiKey;
    private String url;
    @Value("${shiny.connect.okhttp.timeout.seconds}")
    private Integer timeoutSeconds;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
