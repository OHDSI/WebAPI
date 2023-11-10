package org.ohdsi.webapi.statistic.dto;

public class EndpointDto {
    String method;
    String urlPattern;
	
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
}

