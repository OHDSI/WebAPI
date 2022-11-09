package org.ohdsi.webapi.generationcache;

import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.ohdsi.webapi.util.StatementCancel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.ohdsi.webapi.Constants.Params.DESIGN_HASH;
import static org.ohdsi.webapi.Constants.Params.RESULTS_DATABASE_SCHEMA;

@Component
public class CohortGenerationCacheProvider extends AbstractDaoService implements GenerationCacheProvider {
    private static final Logger log = LoggerFactory.getLogger(CohortGenerationCacheProvider.class);

    private static final String CACHE_VALIDATION_TIME = "Checksum of Generation cache for designHash = {} has been calculated in {} milliseconds";

    private static final String COHORT_CHECKSUM_SQL_PATH = "/resources/generationcache/cohort/resultsChecksum.sql";
    private static final String COHORT_RESULTS_SQL = ResourceHelper.GetResourceAsString("/resources/generationcache/cohort/results.sql");
    private static final String CLEANUP_SQL = ResourceHelper.GetResourceAsString("/resources/generationcache/cohort/cleanup.sql");

    @Override
    public boolean supports(CacheableGenerationType type) {

        return Objects.equals(type, CacheableGenerationType.COHORT);
    }

    @Override
    public Integer getDesignHash(String design) {

        // remove elements from object that do not determine results output (names, descriptions, etc)
        CohortExpression cleanExpression = CohortExpression.fromJson(design);
        cleanExpression.title=null;
        cleanExpression.inclusionRules.forEach((rule) -> {
            rule.name = null;
            rule.description = null;
        });
        
        CohortDefinitionDetails cohortDetails = new CohortDefinitionDetails();
        cohortDetails.setExpression(Utils.serialize(cleanExpression));
        return cohortDetails.calculateHashCode();
    }

    @Override
    public String getResultsChecksum(Source source, Integer designHash) {

        long startTime = System.currentTimeMillis();
        PreparedStatementRenderer psr = new PreparedStatementRenderer(
                source,
                COHORT_CHECKSUM_SQL_PATH,
                "@" + RESULTS_DATABASE_SCHEMA,
                SourceUtils.getResultsQualifier(source),
                DESIGN_HASH,
                designHash,
                SessionUtils.sessionId()
        );
        String checksum = getSourceJdbcTemplate(source).queryForObject(psr.getSql(), psr.getOrderedParams(), String.class);
        log.info(CACHE_VALIDATION_TIME, designHash, System.currentTimeMillis() - startTime);
        return checksum;
    }

    @Override
    public String getResultsSql(Integer designHash) {

        return SqlRender.renderSql(
                COHORT_RESULTS_SQL,
                new String[]{DESIGN_HASH},
                new String[]{designHash.toString()}
        );
    }

    @Override
    public void remove(Source source, Integer designHash) {

        String sql = SqlRender.renderSql(
                CLEANUP_SQL,
                new String[]{RESULTS_DATABASE_SCHEMA, DESIGN_HASH},
                new String[]{SourceUtils.getResultsQualifier(source), designHash.toString()}
        );
        sql = SqlTranslate.translateSql(sql, source.getSourceDialect());

        try {
            // StatementCancel parameter is needed for calling batchUpdate of CancelableJdbcTemplate class
            // Without StatementCancel parameter JdbcTemplate.batchUpdate will be used.
            // JdbcTemplate incorrectly determines the support of batch update for impala datasource
            getSourceJdbcTemplate(source).batchUpdate(new StatementCancel(), SqlSplit.splitSql(sql));
        } catch (final Exception e) {
            // if source is unavailable it throws exception that prevents source from being deleted.
            // ignore exception and proceed with deletion.
            log.warn("Cannot remove generation caches from source {}", source.getSourceId());
        }
    }
}
