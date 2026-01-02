package org.ohdsi.webapi.trexsql;

import org.trex.Trexsql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for TrexSQL operations used by SearchProvider.
 */
@Service
@ConditionalOnProperty(name = "trexsql.enabled", havingValue = "true", matchIfMissing = false)
public class TrexsqlService {

    private static final Logger log = LoggerFactory.getLogger(TrexsqlService.class);

    private final TrexsqlConfig config;
    private final TrexsqlInstanceManager instanceManager;

    public TrexsqlService(TrexsqlConfig config, TrexsqlInstanceManager instanceManager) {
        this.config = config;
        this.instanceManager = instanceManager;
    }

    public boolean isEnabledForSource(String sourceKey) {
        return config.isEnabledForSource(sourceKey);
    }

    public boolean isCacheAvailable(String sourceKey) {
        TrexsqlSourceConfig sourceConfig = config.getSourceConfig(sourceKey);
        if (sourceConfig == null) {
            return false;
        }
        String cachePath = config.getCachePath() + "/" + sourceConfig.getDatabaseCode() + ".db";
        return new File(cachePath).exists();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchVocab(String sourceKey, String searchTerm, int maxRows) {
        log.debug("Searching vocabulary for source {} with term: {}", sourceKey, searchTerm);

        TrexsqlSourceConfig sourceConfig = config.getSourceConfig(sourceKey);
        String databaseCode = sourceConfig.getDatabaseCode();

        Map<String, Object> options = new HashMap<>();
        options.put("database-code", databaseCode);
        options.put("max-rows", maxRows);

        try {
            Object db = instanceManager.getInstance();
            List<Map<String, Object>> results = Trexsql.searchVocab(db, searchTerm, options);
            log.debug("Vocabulary search returned {} results", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error searching vocabulary for source {}: {}", sourceKey, e.getMessage(), e);
            throw new RuntimeException("Vocabulary search failed: " + e.getMessage(), e);
        }
    }
}
