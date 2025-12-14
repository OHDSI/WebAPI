package org.ohdsi.webapi.trexsql;

import com.trex.Trexsql;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton manager for the TrexSQL instance.
 * Provides lazy initialization and graceful shutdown.
 */
@Component
@ConditionalOnProperty(name = "trexsql.enabled", havingValue = "true", matchIfMissing = false)
public class TrexsqlInstanceManager {

    private static final Logger log = LoggerFactory.getLogger(TrexsqlInstanceManager.class);

    private final TrexsqlConfig config;
    private volatile Object trexsqlDb = null;
    private final ReentrantLock initLock = new ReentrantLock();

    public TrexsqlInstanceManager(TrexsqlConfig config) {
        this.config = config;
    }

    public Object getInstance() {
        if (!config.isEnabled()) {
            throw new IllegalStateException("Trexsql is not enabled");
        }

        if (trexsqlDb == null) {
            initLock.lock();
            try {
                if (trexsqlDb == null) {
                    log.info("Initializing TrexSQL instance");
                    trexsqlDb = Trexsql.init(buildConfig());
                    log.info("TrexSQL instance initialized successfully");
                }
            } finally {
                initLock.unlock();
            }
        }
        return trexsqlDb;
    }

    public boolean isAvailable() {
        if (!config.isEnabled() || trexsqlDb == null) {
            return false;
        }
        try {
            return Trexsql.isRunning(trexsqlDb);
        } catch (Exception e) {
            log.warn("Error checking TrexSQL status: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAttached(String databaseCode) {
        if (trexsqlDb == null) {
            return false;
        }
        try {
            return Trexsql.isAttached(trexsqlDb, databaseCode);
        } catch (Exception e) {
            log.warn("Error checking if database {} is attached: {}", databaseCode, e.getMessage());
            return false;
        }
    }

    private Map<String, Object> buildConfig() {
        Map<String, Object> initConfig = new HashMap<>();

        if (config.getExtensionsPath() != null && !config.getExtensionsPath().isEmpty()) {
            initConfig.put("extensions-path", config.getExtensionsPath());
        }

        return initConfig;
    }

    @PreDestroy
    public void shutdown() {
        if (trexsqlDb != null) {
            log.info("Shutting down TrexSQL instance");
            try {
                Trexsql.shutdown(trexsqlDb);
                log.info("TrexSQL instance shut down successfully");
            } catch (Exception e) {
                log.error("Error shutting down TrexSQL instance: {}", e.getMessage(), e);
            } finally {
                trexsqlDb = null;
            }
        }
    }
}
