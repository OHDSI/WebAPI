package com.jnj.honeur.webapi.hss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jnj.honeur.security.TokenContext;
import com.jnj.honeur.webapi.cohortdefinition.CohortGenerationResults;
import com.jnj.honeur.webapi.hssserviceuser.HSSServiceUserEntity;
import com.jnj.honeur.webapi.hssserviceuser.HSSServiceUserRepository;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

@Component
public class StorageServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageServiceClient.class);

    private RestTemplate restTemplate;

    @Autowired
    private HSSServiceUserRepository hssServiceUserRepository;

    @Value("${datasource.hss.url}")
    private String storageServiceApi;

    @Value("${webapi.central}")
    private boolean webapiCentral;

    @Value("${security.token.expiration}")
    private int EXPIRATION_TIME;


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

    public void setStorageServiceApi(String storageServiceApi) {
        this.storageServiceApi = storageServiceApi;
    }
    public void setWebapiCentral(boolean webapiCentral) {
        this.webapiCentral = webapiCentral;
    }
    public void setHssServiceUserRepository(HSSServiceUserRepository hssServiceUserRepository) {
        this.hssServiceUserRepository = hssServiceUserRepository;
    }

    public String saveResults(String token, String fingerprint, File results, String uuid) {
        if (!webapiCentral) {
            TokenContext tokenContext = getStorageServiceToken();
            token = tokenContext.getToken();
            fingerprint = tokenContext.getFingerprint();
        }
        String endpoint = "/cohort-results/" + uuid;
        try {
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = createHttpEntity(token, fingerprint, results);

            String result = restTemplate.exchange(storageServiceApi + endpoint,
                    HttpMethod.POST, requestEntity, String.class).getHeaders().getLocation().getPath();

            LOGGER.info(String.format("Cohort results posted to HSS @ %s: %s", (storageServiceApi + endpoint), result));

            return result;
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean saveCohort(String token, String fingerprint, File results, final UUID groupKey, final UUID uuid) {
        final String endpoint = "/cohort-definitions/" + groupKey + "/" + uuid;

        try {
            if (!webapiCentral) {
                TokenContext tokenContext = getStorageServiceToken();
                token = tokenContext.getToken();
                fingerprint = tokenContext.getFingerprint();
            }

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = createHttpEntity(token, fingerprint, results);

            final ResponseEntity<StorageInformationItem> response = restTemplate.exchange(storageServiceApi + endpoint,
                    HttpMethod.POST, requestEntity, StorageInformationItem.class);

            LOGGER.info("Response status code: " + response.getStatusCode());
            LOGGER.info("Response body: " + response.getBody());
            LOGGER.info(String.format("Cohort definition posted to HSS @ %s: %s", (storageServiceApi + endpoint), response));

            return true;
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    private HttpEntity<LinkedMultiValueMap<String, Object>> createHttpEntity(String token, String fingerprint, File file) {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new FileSystemResource(file.getAbsolutePath()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("token", token.replace("Bearer ", ""));
        headers.set("Cookie", "userFingerprint="+fingerprint);

        return new HttpEntity<>(map, headers);
    }

    public List<CohortDefinitionStorageInformationItem> getCohortDefinitionImportList(String token, String fingerprint) {
        if (!webapiCentral) {
            TokenContext tokenContext = getStorageServiceToken();
            token = tokenContext.getToken();
            fingerprint = tokenContext.getFingerprint();
        }
        String endpoint = "/cohort-definitions/list";

        return Arrays.asList(restTemplate.exchange(storageServiceApi + endpoint, HttpMethod.GET,
                getTokenHeader(token, fingerprint), CohortDefinitionStorageInformationItem[].class).getBody());
    }

    public CohortDefinitionService.CohortDefinitionDTO getCohortDefinition(String token, String fingerprint, String uuid) {
        if (!webapiCentral) {
            TokenContext tokenContext = getStorageServiceToken();
            token = tokenContext.getToken();
            fingerprint = tokenContext.getFingerprint();
        }
        String endpoint = "/cohort-definitions/" + uuid;
        return restTemplate
                .exchange(storageServiceApi + endpoint, HttpMethod.GET, getTokenHeader(token, fingerprint), CohortDefinitionService.CohortDefinitionDTO.class)
                .getBody();
    }

    public List<StorageInformationItem> getCohortDefinitionResultsImportList(String token, String fingerprint, UUID uuid) {
        if (!webapiCentral) {
            TokenContext tokenContext = getStorageServiceToken();
            token = tokenContext.getToken();
            fingerprint = tokenContext.getFingerprint();
        }
        try {
            String endpoint = "/cohort-results/list/" + uuid + "?reverseOrder=true";
            return Arrays.asList(restTemplate
                    .exchange(storageServiceApi + endpoint, HttpMethod.GET, getTokenHeader(token, fingerprint),
                            StorageInformationItem[].class).getBody());
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public CohortGenerationResults getCohortGenerationResults(String token, String fingerprint, String definitionUuid, String resultsUuid) throws IOException {
        if (!webapiCentral) {
            TokenContext tokenContext = getStorageServiceToken();
            token = tokenContext.getToken();
            fingerprint = tokenContext.getFingerprint();
        }
        String endpoint = "/cohort-results/" + definitionUuid + "/" + resultsUuid;
        String response = restTemplate
                .exchange(storageServiceApi + endpoint, HttpMethod.GET, getTokenHeader(token, fingerprint), String.class)
                .getBody();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, CohortGenerationResults.class);
    }

    public TokenContext getStorageServiceToken() {
        try{
            ResponseEntity<JsonNode> tokenResponse = restTemplate
                    .exchange(storageServiceApi + "/login", HttpMethod.GET, getBasicAuthenticationHeader(),
                            JsonNode.class);
            return new TokenContext(tokenResponse.getBody().path("token").asText(), tokenResponse.getHeaders().getFirst("Set-Cookie").replace("userFingerprint=", ""));
        } catch (Exception e) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "CAS login failed: Wrong storage service user credentials");
        }
    }

    boolean deleteStorageFile(String token, String fingerprint, String uuid) {
        try {
            String serviceUrl = storageServiceApi + "/" + uuid;
            restTemplate.exchange(serviceUrl, HttpMethod.DELETE, getTokenHeader(token, fingerprint), String.class);
            return true;
        } catch (RestClientException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    private HttpEntity getTokenHeader(String token, String fingerprint) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token.replace("Bearer ", ""));
        headers.set("Cookie", "userFingerprint="+fingerprint);

        return new HttpEntity(headers);
    }

    private HttpEntity getBasicAuthenticationHeader() {
        Iterator<HSSServiceUserEntity> hssServiceUserEntities = hssServiceUserRepository.findAll().iterator();
        if(!hssServiceUserEntities.hasNext()){
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN,"No HSS service user defined.");
        }
        final HSSServiceUserEntity hssServiceUser = hssServiceUserEntities.next();
        return new HttpEntity(new HttpHeaders() {{
            String auth = hssServiceUser.getUsername() + ":" + hssServiceUser.getPlainTextPassword();
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }});
    }

    public int healthCheck() {
        final RestTemplate restTemplate = new RestTemplate();
        String serviceUrl = storageServiceApi + "/health-status?transitive=false";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(serviceUrl, String.class);
            return response.getStatusCode().value();
        } catch (RestClientException e) {
            LOGGER.warn(e.getMessage(), e);
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
    }
}
