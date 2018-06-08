package com.jnj.honeur.webapi.hss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jnj.honeur.webapi.cohortdefinition.CohortGenerationResults;
import com.jnj.honeur.webapi.liferay.LiferayApiClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class StorageServiceClient extends RestTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiferayApiClient.class);

    private RestTemplate restTemplate;

    @Value("${datasource.hss.url}")
    private String STORAGE_SERVICE_API;


    public StorageServiceClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        restTemplate = new RestTemplate(requestFactory);
    }

    public void saveResults(final String token, File results, String uuid) {
        String endpoint = "/cohort-results/" + uuid;
        try {
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = createHttpEntity(token, results);

            restTemplate.exchange(STORAGE_SERVICE_API + endpoint,
                    HttpMethod.POST, requestEntity, String.class).getBody();
        } catch (HttpStatusCodeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public String saveCohort(final String token, File results, final UUID groupKey) {
        String endpoint = "/cohort-definitions/"+groupKey;
        try {
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = createHttpEntity(token, results);

            return restTemplate.exchange(STORAGE_SERVICE_API + endpoint,
                    HttpMethod.POST, requestEntity, StorageInformationItem.class).getHeaders()
                    .getLocation().getPath().replace("/cohort-definitions/", "");
        } catch (HttpStatusCodeException e){
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private HttpEntity<LinkedMultiValueMap<String, Object>> createHttpEntity(String token, File file) {

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new FileSystemResource(file.getAbsolutePath()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("token", token.replace("Bearer ", ""));

        return new HttpEntity<>(map, headers);
    }

    public List<CohortDefinitionStorageInformationItem> getCohortDefinitionImportList(final String token) {
        String endpoint = "/cohort-definitions";

        return Arrays.asList(restTemplate.exchange(STORAGE_SERVICE_API + endpoint, HttpMethod.GET,
                getTokenHeader(token), CohortDefinitionStorageInformationItem[].class).getBody());
    }

    public String getCohortDefinition(final String token, String uuid) {
        String endpoint = "/cohort-definitions/" + uuid;
        return restTemplate.exchange(STORAGE_SERVICE_API + endpoint, HttpMethod.GET, getTokenHeader(token), JsonNode.class).getBody().asText();
    }

    public List<StorageInformationItem> getCohortDefinitionResultsImportList(final String token, UUID uuid) {
        String endpoint = "/cohort-results/"+uuid+"?reverseOrder=true";
        return Arrays.asList(restTemplate.exchange(STORAGE_SERVICE_API + endpoint, HttpMethod.GET, getTokenHeader(token), StorageInformationItem[].class).getBody());
    }

    public CohortGenerationResults getCohortGenerationResults(final String token, String definitionUuid, String resultsUuid) throws IOException {
        String endpoint = "/cohort-results/"+definitionUuid+"/"+resultsUuid;
        String response = restTemplate.exchange(STORAGE_SERVICE_API + endpoint, HttpMethod.GET, getTokenHeader(token), String.class).getBody();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, CohortGenerationResults.class);
    }

    private HttpEntity getTokenHeader(final String token){
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token.replace("Bearer ", ""));

        return new HttpEntity(headers);
    }


}
