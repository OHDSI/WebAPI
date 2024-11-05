package org.ohdsi.webapi.cdmresults.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.ohdsi.webapi.cdmresults.domain.CDMCacheEntity;
import org.ohdsi.webapi.cdmresults.repository.CDMCacheRepository;
import org.springframework.core.convert.ConversionService;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;

import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceHelper;

@RunWith(MockitoJUnitRunner.class)
public class CDMCacheServiceTest {

    private static final String PGSQL_CONN_STR = "jdbc:postgresql://localhost:5432/ohdsi?ssl=true&user=user&password=secret";

    @Mock
    private CDMCacheRepository cdmCacheRepositoryMock;

    @Mock
    private CDMCacheBatchService batchServiceMock;

    @Mock
    private ConversionService conversionServiceMock;

    @Mock
    private SourceHelper sourceHelper = new SourceHelper();

    @InjectMocks
    private CDMCacheService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        assertNotNull(service);
    }

    @Test
    public void findAndCache_nothingCached_fetchesItemsAndReturnsThem() {

        // Arrange
        List<Integer> ids = Arrays.asList(1, 2, 3, 4);
        Source source = getCdmSource();

        when(cdmCacheRepositoryMock.findBySourceAndConceptIds(0, ids)).thenReturn(new ArrayList<CDMCacheEntity>());
        when(sourceHelper.getSourceConnectionString(source)).thenReturn(PGSQL_CONN_STR);

        // Act
        List<CDMCacheEntity> result = service.findAndCache(source, ids);

        // Assert
        assertEquals(4, result.size());

        // cdmCacheMock.when()
        // when(function.apply(any())).thenReturn(
        // ids.stream().map(CDMResultsCacheTest.this::createDescendantRecordCount).collect(Collectors.toList()));

        // Collection<DescendantRecordCount> records = cache.findAndCache(ids,
        // function);
        // assertEquals(4, records.size());
        // assertEquals(ids,
        // records.stream().map(DescendantRecordCount::getId).collect(Collectors.toList()));
    }

    protected final String SOURCE_KEY = "Embedded_PG";
    protected static final String CDM_SCHEMA_NAME = "cdm";
    protected static final String RESULT_SCHEMA_NAME = "results";

    private Source getCdmSource() {
        Source source = new Source();
        source.setSourceName("Embedded PG");
        source.setSourceKey(SOURCE_KEY);
        source.setSourceDialect(DBMSType.POSTGRESQL.getOhdsiDB());
        source.setSourceConnection(PGSQL_CONN_STR);
        source.setUsername("postgres");
        source.setPassword("postgres");
        source.setKrbAuthMethod(KerberosAuthMechanism.PASSWORD);

        SourceDaimon cdmDaimon = new SourceDaimon();
        cdmDaimon.setPriority(1);
        cdmDaimon.setDaimonType(SourceDaimon.DaimonType.CDM);
        cdmDaimon.setTableQualifier(CDM_SCHEMA_NAME);
        cdmDaimon.setSource(source);

        SourceDaimon vocabDaimon = new SourceDaimon();
        vocabDaimon.setPriority(1);
        vocabDaimon.setDaimonType(SourceDaimon.DaimonType.Vocabulary);
        vocabDaimon.setTableQualifier(CDM_SCHEMA_NAME);
        vocabDaimon.setSource(source);

        SourceDaimon resultsDaimon = new SourceDaimon();
        resultsDaimon.setPriority(1);
        resultsDaimon.setDaimonType(SourceDaimon.DaimonType.Results);
        resultsDaimon.setTableQualifier(RESULT_SCHEMA_NAME);
        resultsDaimon.setSource(source);

        source.setDaimons(Arrays.asList(cdmDaimon, vocabDaimon, resultsDaimon));

        return source;
    }

}
