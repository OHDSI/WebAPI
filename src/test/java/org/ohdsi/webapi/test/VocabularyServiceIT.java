package org.ohdsi.webapi.test;

import com.odysseusinc.arachne.commons.types.DBMSType;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

public class VocabularyServiceIT extends WebApiIT {
    private static final String CONCEPT_FILE_PATH = "/database/concept.sql";

    @Value("${vocabularyservice.endpoint.vocabularies}")
    private String endpointVocabularies;

    @Value("${vocabularyservice.endpoint.concept}")
    private String endpointConcept;

    @Value("${vocabularyservice.endpoint.domains}")
    private String endpointDomains;

    @Autowired
    private SourceRepository sourceRepository;

    @Before
    public void init() throws Exception {
        truncateTable(String.format("%s.%s", "public", "source"));
        resetSequence(String.format("%s.%s", "public", "source_sequence"));
        sourceRepository.saveAndFlush(getCdmSource());
        prepareCdmSchema();
        prepareResultSchema();
        addConcept();
    }

    private void addConcept() {
        String resultSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(CONCEPT_FILE_PATH),
                new String[]{"cdm_database_schema"}, new String[]{CDM_SCHEMA_NAME});
        String sql = SqlTranslate.translateSql(resultSql, DBMSType.POSTGRESQL.getOhdsiDB());
        jdbcTemplate.execute(sql);
    }

    @Test
    public void canGetConcept() {

        //Action
        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointConcept, String.class);
        
        //Assertion
        assertOK(entity);
    }

    @Test
    public void canGetVocabularies() {

        //Action
        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointVocabularies, String.class);
        
        //Assertion
        assertOK(entity);
    }

    @Test
    public void canGetDomains() {

        //Action
        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointDomains, String.class);

        //Assertion
        assertOK(entity);
    }
}
