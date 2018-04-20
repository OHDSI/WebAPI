package org.ohdsi.webapi.service;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.springframework.stereotype.Component;

@Component
public class HttpClient {
    
    private Client client;
    
    @PostConstruct
    private void init() throws KeyManagementException, NoSuchAlgorithmException {
        this.client = getClient();
    }
    
    private Client getClient() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, null);
        return ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .register(MultiPartFeature.class)
                .build();
    }

    public WebTarget target(final String executionEngineURL) {

        return client.target(executionEngineURL);
    }
}
