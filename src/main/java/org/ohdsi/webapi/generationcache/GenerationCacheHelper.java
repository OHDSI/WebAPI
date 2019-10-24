package org.ohdsi.webapi.generationcache;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationRequest;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationRequestBuilder;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationUtils;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.SourceUtils;
import org.ohdsi.webapi.util.StatementCancel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static org.ohdsi.webapi.Constants.Params.RESULTS_DATABASE_SCHEMA;

@Component
public class GenerationCacheHelper {

    private static final Logger log = LoggerFactory.getLogger(GenerationCacheHelper.class);
    private static final String CACHE_USED = "Using cached generation results for %s with id=%s and source=%s";
    private static final ConcurrentHashMap<CacheableResource, Object> monitors = new ConcurrentHashMap<>();

    private final GenerationCacheService generationCacheService;

    public GenerationCacheHelper(GenerationCacheService generationCacheService) {

        this.generationCacheService = generationCacheService;
    }

    public CacheResult computeCacheIfAbsent(CohortDefinition cohortDefinition, Source source, CohortGenerationRequestBuilder requestBuilder, BiConsumer<Integer, String[]> sqlExecutor) {

        CacheableGenerationType type = CacheableGenerationType.COHORT;
        String designHash = generationCacheService.getDesignHash(type, cohortDefinition.getDetails().getExpression());

        log.info("Computes cache if absent for type = {}, design = {}, source id = {}", type, designHash, source.getSourceId());

        synchronized (monitors.computeIfAbsent(new CacheableResource(type, designHash, source.getSourceId()), cr -> new Object())) {
            log.info("Retrieves or invalidates cache for cohort id = {}", cohortDefinition.getId());
            GenerationCache cache = generationCacheService.getCacheOrEraseInvalid(type, designHash, source.getSourceId());
            if (cache == null) {
                Integer resultIdentifier = generationCacheService.getNextResultIdentifier(type, source);
                log.info("Cache is absent for cohort id = {}. Calculating with results identifier = {}", cohortDefinition.getId(), resultIdentifier);
                // Ensure that there are no records in results schema with which we could mess up
                generationCacheService.removeCache(type, source, resultIdentifier);
                CohortGenerationRequest cohortGenerationRequest = requestBuilder
                    .withExpression(cohortDefinition.getDetails().getExpressionObject())
                    .withSource(source)
                    .withTargetId(resultIdentifier)
                    .build();
                String[] sqls = CohortGenerationUtils.buildGenerationSql(cohortGenerationRequest);
                sqlExecutor.accept(resultIdentifier, sqls);
                cache = generationCacheService.cacheResults(CacheableGenerationType.COHORT, designHash, source.getSourceId(), resultIdentifier);
            } else {
                log.info(String.format(CACHE_USED, type, cohortDefinition.getId(), source.getSourceKey()));
            }
            String sql = SqlRender.renderSql(
                    generationCacheService.getResultsSql(cache),
                    new String[]{RESULTS_DATABASE_SCHEMA},
                    new String[]{SourceUtils.getResultsQualifier(source)}
            );
            log.info("Finished computation cache if absent for cohort id = {}", cohortDefinition.getId());
            return new CacheResult(cache.getResultIdentifier(), sql);
        }
    }

    public void runCancelableCohortGeneration(CancelableJdbcTemplate cancelableJdbcTemplate, StatementCancel stmtCancel, String sqls[]) {

        cancelableJdbcTemplate.batchUpdate(stmtCancel, sqls);
        // Ensure that no cache created if generation has been cancelled
        if (stmtCancel.isCanceled()) {
            throw new RuntimeException("Cohort generation has been cancelled");
        }
    }

    public class CacheResult {

        private Integer identifier;
        private String sql;

        public CacheResult(Integer identifier, String sql) {

            this.identifier = identifier;
            this.sql = sql;
        }

        public Integer getIdentifier() {

            return identifier;
        }

        public String getSql() {

            return sql;
        }
    }

    private static class CacheableResource {

        private CacheableGenerationType type;
        private String designHash;
        private Integer sourceId;

        public CacheableResource(CacheableGenerationType type, String designHash, Integer sourceId) {

            this.type = type;
            this.designHash = designHash;
            this.sourceId = sourceId;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheableResource that = (CacheableResource) o;
            return type == that.type &&
                    Objects.equals(designHash, that.designHash) &&
                    Objects.equals(sourceId, that.sourceId);
        }

        @Override
        public int hashCode() {

            return Objects.hash(type, designHash, sourceId);
        }
    }
}
