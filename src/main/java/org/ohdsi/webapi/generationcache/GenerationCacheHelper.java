package org.ohdsi.webapi.generationcache;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.SourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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

    public String computeIfAbsent(CohortDefinition cohortDefinition, Source source, Consumer<Integer> generateCohort) {

        CacheableGenerationType type = CacheableGenerationType.COHORT;
        String designHash = generationCacheService.getDesignHash(type, cohortDefinition.getDetails().getExpression());

        synchronized (monitors.computeIfAbsent(new CacheableResource(type, designHash, source.getSourceId()), cr -> new Object())) {
            GenerationCache cache = generationCacheService.getCache(type, designHash, source.getSourceId());
            if (cache == null) {
                Integer resultIdentifier = generationCacheService.getNextResultIdentifier(type, source);
                generateCohort.accept(resultIdentifier);
                cache = generationCacheService.cacheResults(CacheableGenerationType.COHORT, designHash, source.getSourceId(), resultIdentifier);
            } else {
                log.info(String.format(CACHE_USED, type, cohortDefinition.getId(), source.getSourceKey()));
            }
            return SqlRender.renderSql(
                    generationCacheService.getResultsSql(cache),
                    new String[]{RESULTS_DATABASE_SCHEMA},
                    new String[]{SourceUtils.getResultsQualifier(source)}
            );
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
