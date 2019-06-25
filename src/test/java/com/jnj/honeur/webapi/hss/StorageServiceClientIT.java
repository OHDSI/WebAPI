package com.jnj.honeur.webapi.hss;

import com.jnj.honeur.security.TokenContext;
import com.jnj.honeur.webapi.cohortdefinition.CohortGenerationResults;
import com.jnj.honeur.webapi.hssserviceuser.HSSServiceUserEntity;
import com.jnj.honeur.webapi.hssserviceuser.HSSServiceUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StorageServiceClientIT {

    private StorageServiceClient storageServiceClient;
    private String token;
    private String fingerprint;

    @Before
    public void setup() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        storageServiceClient = new StorageServiceClient();
        storageServiceClient.setHssServiceUserRepository(mockHSSServiceUserRepository());
        storageServiceClient.setStorageServiceApi("https://dev.honeur.org/storage/api");
        storageServiceClient.setWebapiCentral(true);

        TokenContext tokenContext = storageServiceClient.getStorageServiceToken();
        token = tokenContext.getToken();
        fingerprint = tokenContext.getFingerprint();
    }

    private HSSServiceUserRepository mockHSSServiceUserRepository() {
        HSSServiceUserEntity serviceUser = new HSSServiceUserEntity();
        serviceUser.setUsername("pmoorth1@its.jnj.com");
        serviceUser.setPlainTextPassword("test");

        HSSServiceUserRepository repo = mock(HSSServiceUserRepository.class);
        when(repo.findAll()).thenReturn(Collections.singleton(serviceUser));

        return repo;
    }

    @Test
    public void getStorageServiceToken() {
        TokenContext tokenContext = storageServiceClient.getStorageServiceToken();
        token = tokenContext.getToken();
        fingerprint = tokenContext.getFingerprint();
        assertNotNull(token);
        assertTrue(token.length() > 20);
    }

    @Test
    public void getCohortDefinitionImportList() {
        List<CohortDefinitionStorageInformationItem> cohortDefinitionInfoList = storageServiceClient.getCohortDefinitionImportList(token, fingerprint);
        assertNotNull(cohortDefinitionInfoList);
        assertTrue(cohortDefinitionInfoList.size() > 0);
    }

    @Test
    public void getCohortDefinition() {
        List<CohortDefinitionStorageInformationItem> cohortDefinitionInfoList = storageServiceClient.getCohortDefinitionImportList(token, fingerprint);
        assertNotNull(cohortDefinitionInfoList);
        assertTrue(cohortDefinitionInfoList.size() > 0);

        String uuid = null;
        CohortDefinitionService.CohortDefinitionDTO cohortDefinition = null;

        for (CohortDefinitionStorageInformationItem cohortDefinitionStorageInformationItem: cohortDefinitionInfoList) {
            try {
                uuid = cohortDefinitionStorageInformationItem.getUuid();
                cohortDefinition = storageServiceClient.getCohortDefinition(token, fingerprint, uuid);
                if(cohortDefinition != null) {
                    System.out.println("Valid cohort definition retrieved!");
                    break;
                }
            } catch (HttpMessageNotReadableException e) {
                //Loop for finding correct data amongst legacy data.
                System.err.println("Legacy cohort definition with UUID: " + uuid);
            } catch (RestClientException e) {
                //Loop for finding correct data amongst legacy data.
                System.err.println("Error on cohort definition with UUID " + uuid + ": " + e.getMessage());
            }
        }

        assertNotNull("No correct data found or test user has no access to correct data", cohortDefinition);
        assertNotNull("No correct data found or test user has no access to correct data", uuid);
        assertNotNull(cohortDefinition.name);
        assertNotNull(cohortDefinition.expression);
        assertEquals(UUID.fromString(uuid), cohortDefinition.uuid);
    }

    @Test
    public void saveCohort() throws URISyntaxException {
        UUID groupKey = UUID.randomUUID();
        UUID uuid = UUID.randomUUID();
        File cohortFile = new File(getClass().getClassLoader().getResource("hypertension.cohort").toURI());
        assertTrue(storageServiceClient.saveCohort(token, fingerprint, cohortFile, groupKey, uuid));
        assertTrue(storageServiceClient.deleteStorageFile(token, fingerprint, uuid.toString()));
    }

    @Test
    public void saveResults() throws URISyntaxException {
        List<CohortDefinitionStorageInformationItem> cohortDefinitionInfoList = storageServiceClient.getCohortDefinitionImportList(token, fingerprint);
        assertNotNull(cohortDefinitionInfoList);
        assertTrue(cohortDefinitionInfoList.size() > 0);

        String cohortDefinitionUuid = cohortDefinitionInfoList.get(0).getUuid();

        // Save cohort result
        File resultsFile = new File(getClass().getClassLoader().getResource("hypertension.result").toURI());
        String newCohortResultPath = storageServiceClient.saveResults(token, fingerprint, resultsFile, cohortDefinitionUuid);
        assertNotNull(newCohortResultPath);

        // Cleanup
        assertTrue(storageServiceClient.deleteStorageFile(token, fingerprint, parseUuid(newCohortResultPath)));
    }

    @Test
    public void getCohortDefinitionResultsImportList() {
        List<CohortDefinitionStorageInformationItem> cohortDefinitionInfoList = storageServiceClient.getCohortDefinitionImportList(token, fingerprint);
        assertNotNull(cohortDefinitionInfoList);
        assertTrue(cohortDefinitionInfoList.size() > 0);

        boolean resultFound = false;
        for(CohortDefinitionStorageInformationItem cohortDefinitionInfo : cohortDefinitionInfoList) {
            List<StorageInformationItem> resultsImportList = storageServiceClient.getCohortDefinitionResultsImportList(token, fingerprint, UUID.fromString(cohortDefinitionInfo.getUuid()));
            assertNotNull(resultsImportList);
            for(StorageInformationItem cohortResultInfo:resultsImportList) {
                assertTrue(cohortResultInfo.getKey().contains(cohortDefinitionInfo.getUuid()));
                resultFound = true;
            }
        }
        assertTrue(resultFound);
    }

    @Test
    public void getCohortGenerationResults() throws IOException {
        CohortDefinitionStorageInformationItem cohortDefinitionInfo = getCohortDefinitionWithResultsInfo();
        assertNotNull(cohortDefinitionInfo);
        StorageInformationItem cohortResultInfo = getCohortResultInfo(cohortDefinitionInfo);
        assertNotNull(cohortResultInfo);
        CohortGenerationResults cohortGenerationResults = storageServiceClient.getCohortGenerationResults(token, fingerprint, cohortDefinitionInfo.getUuid(), cohortResultInfo.getUuid());
        assertNotNull(cohortGenerationResults);
    }

    private CohortDefinitionStorageInformationItem getCohortDefinitionWithResultsInfo() {
        List<CohortDefinitionStorageInformationItem> cohortDefinitionInfoList = storageServiceClient.getCohortDefinitionImportList(token, fingerprint);
        for(CohortDefinitionStorageInformationItem cohortDefinitionInfo : cohortDefinitionInfoList) {
            List<StorageInformationItem> resultsImportList = storageServiceClient.getCohortDefinitionResultsImportList(token, fingerprint, UUID.fromString(cohortDefinitionInfo.getUuid()));
            if(resultsImportList.size() > 0) {
                return cohortDefinitionInfo;
            }
        }
        return null;
    }

    private StorageInformationItem getCohortResultInfo(CohortDefinitionStorageInformationItem cohortDefinitionInfo) {
        List<StorageInformationItem> resultsImportList = storageServiceClient.getCohortDefinitionResultsImportList(token, fingerprint, UUID.fromString(cohortDefinitionInfo.getUuid()));
        if(resultsImportList.size() > 0) {
            return resultsImportList.get(0);
        }
        return null;
    }

    private String parseUuid(String location) {
        if(location.indexOf('/') != -1) {
            return location.substring(location.lastIndexOf('/') + 1);
        } else {
            return location;
        }
    }


}