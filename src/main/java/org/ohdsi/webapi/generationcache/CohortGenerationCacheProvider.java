package org.ohdsi.webapi.generationcache;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CohortGenerationCacheProvider extends AbstractDaoService implements GenerationCacheProvider {

    private static final String COHORT_CHECKSUM_SQL_PATH = "/resources/generationcache/cohortResultsChecksum.sql";
    private static final String NEXT_ID_SQL_PATH = "/resources/generationcache/nextResultIdentifier.sql";
    private static final String COHORT_RESULTS_SQL = ResourceHelper.GetResourceAsString("/resources/generationcache/cohortResults.sql");
    private static final String RESULTS_DATABASE_SCHEMA = "@results_database_schema";
    private static final String RESULTS_IDENTIFIER = "cohort_definition_id";

    @Override
    public boolean supports(CacheableGenerationType type) {

        return Objects.equals(type, CacheableGenerationType.COHORT);
    }

    @Override
    public String getDesignHash(String design) {

        CohortDefinitionDetails cohortDetails = new CohortDefinitionDetails();
        cohortDetails.setExpression(design);
        return cohortDetails.calculateHashCode().toString();
    }

    @Override
    public Integer getNextResultIdentifier(Source source) {

        PreparedStatementRenderer psr = new PreparedStatementRenderer(
            source,
            NEXT_ID_SQL_PATH,
            RESULTS_DATABASE_SCHEMA,
            SourceUtils.getResultsQualifier(source)
        );
        return getSourceJdbcTemplate(source).queryForObject(psr.getSql(), psr.getOrderedParams(), Integer.class);
    }

    @Override
    public String getResultsChecksum(Source source, Integer resultIdentifier) {

        PreparedStatementRenderer psr = new PreparedStatementRenderer(
            source,
            COHORT_CHECKSUM_SQL_PATH,
            RESULTS_DATABASE_SCHEMA,
            SourceUtils.getResultsQualifier(source),
            RESULTS_IDENTIFIER,
            resultIdentifier,
            SessionUtils.sessionId()
        );
        return getSourceJdbcTemplate(source).queryForObject(psr.getSql(), psr.getOrderedParams(), String.class);
    }

    @Override
    public String getResultsSql(Integer resultIdentifier) {

        return SqlRender.renderSql(
                COHORT_RESULTS_SQL,
                new String[]{RESULTS_IDENTIFIER},
                new String[]{resultIdentifier.toString()}
        );
    }
}
