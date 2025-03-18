package org.ohdsi.webapi.statistic.dto;

public class EndpointDto {
    String method;
    String urlPattern;
    String userId;
	
    public String getMethod() {
        return method;
    }
	
    public void setMethod(String method) {
        this.method = method;
	}
    
	public String getUrlPattern() {
        return urlPattern;
	}
	
    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

