package org.ohdsi.webapi.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.cache.CacheService;
import org.ohdsi.webapi.report.CDMAttribute;
import org.ohdsi.webapi.report.CDMDashboard;
import org.ohdsi.webapi.report.CDMResultsAnalysisRunner;
import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * We mocked data source repositories, whenever it necessary.
 * We did it just because we don't have test DB for the data source, as soon we add such DB we can get rid of the mocks
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CacheServiceIT {

    @MockBean
    private CDMResultsAnalysisRunner queryRunner;
    @MockBean
    private SourceRepository sourceRepository;
    @MockBean
    private SourceService sourceService;

    @Autowired
    private CDMResultsService resultsService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private Source source;
    private CDMAttribute cdmAttribute1;
    private CDMAttribute cdmAttribute2;
    private CDMDashboard dashboard1;
    private CDMDashboard dashboard2;

    @BeforeClass
    public static void prepareGroup() {

        TomcatURLStreamHandlerFactory.disable();
    }

    @Before
    public void setUp() {

        source = createSource(1, "source1", "name");
        cdmAttribute1 = createCdmAttribute("summeryKey1", "summeryValue1");
        cdmAttribute2 = createCdmAttribute("summeryKey2", "summeryValue2");
        dashboard1 = createDashboard(cdmAttribute1);
        dashboard2 = createDashboard(cdmAttribute2);

        doReturn(this.source).when(sourceRepository).findBySourceKey(eq(source.getSourceKey()));
        doReturn(CollectionUtils.emptyCollection()).when(sourceService).getSources();
    }

    @Test
    public void inspectCaches() {

        when(queryRunner.getDashboard(any(), any())).thenReturn(dashboard1);
        resultsService.warmCaches(source, this.resultsService);

        String endpoint = formatEndpointString(this.port, source.getSourceKey());
        CDMDashboard dashboardFromRequest = restTemplate.getForEntity(endpoint, CDMDashboard.class).getBody();
        assertThat(
                Collections.singletonList(cdmAttribute1),
                equalTo(dashboardFromRequest.getSummary())
        );

        when(queryRunner.getDashboard(any(), any())).thenReturn(dashboard2);
        dashboardFromRequest = restTemplate.getForEntity(endpoint, CDMDashboard.class).getBody();
        assertThat(
                Collections.singletonList(cdmAttribute1),
                equalTo(dashboardFromRequest.getSummary())
        );

        cacheService.inspectCaches();
        dashboardFromRequest = restTemplate.getForEntity(endpoint, CDMDashboard.class).getBody();
        assertThat(
                Collections.singletonList(cdmAttribute2),
                equalTo(dashboardFromRequest.getSummary())
        );
    }


    private Source createSource(int sourceId, String sourceKey, String sourceName) {

        Source source = new Source();
        source.setSourceId(sourceId);
        source.setSourceKey(sourceKey);
        source.setSourceName(sourceName);
        source.setSourceDialect("postgresql");
        source.setSourceConnection("jdbc:postgresql://cdm:5432/synpuf");
        source.setDaimons(Collections.emptyList());
        return source;
    }

    private CDMDashboard createDashboard(CDMAttribute summaryAttribute) {

        CDMDashboard cdmDashboard = new CDMDashboard();
        cdmDashboard.setSummary(Collections.singletonList(summaryAttribute));
        return cdmDashboard;

    }

    private CDMAttribute createCdmAttribute(String name, String value) {

        CDMAttribute attribute = new CDMAttribute();
        attribute.setAttributeName(name);
        attribute.setAttributeValue(value);
        return attribute;
    }

    private String formatEndpointString(int port, String sourceKey) {

        return "http://localhost:" + port + "/WebAPI/cdmresults/" + sourceKey + "/dashboard";
    }

}

