package org.ohdsi.webapi.trexsql;

import org.trex.Trexsql;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Component
@ConditionalOnProperty(name = "trexsql.enabled", havingValue = "true", matchIfMissing = false)
public class TrexSQLInstanceManager {

    private static final Logger log = LoggerFactory.getLogger(TrexSQLInstanceManager.class);

    private final TrexSQLConfig config;
    private volatile boolean initialized = false;
    private volatile boolean initFailed = false;
    private final ReentrantLock initLock = new ReentrantLock();

    public TrexSQLInstanceManager(TrexSQLConfig config) {
        this.config = config;
    }

    public void ensureInitialized() {
        if (!config.isEnabled()) {
            throw new IllegalStateException("TrexSQL is not enabled");
        }

        if (initFailed) {
            return;
        }

        if (!initialized) {
            initLock.lock();
            try {
                if (!initialized && !initFailed) {
                    log.info("Initializing TrexSQL instance");
                    try {
                        Trexsql.init(buildConfig());
                        initialized = true;
                        log.info("TrexSQL instance initialized successfully");
                    } catch (Exception | Error e) {
                        log.error("Failed to initialize TrexSQL: {}. TrexSQL features will be unavailable.", e.getMessage());
                        initFailed = true;
                    }
                }
            } finally {
                initLock.unlock();
            }
        }
    }

    public boolean isAvailable() {
        if (!config.isEnabled() || !initialized) {
            return false;
        }
        try {
            return Trexsql.isRunning();
        } catch (Exception e) {
            log.warn("Error checking TrexSQL status: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAttached(String databaseCode) {
        if (!initialized) {
            return false;
        }
        try {
            return Trexsql.isAttached(databaseCode);
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

        if (config.getCachePath() != null && !config.getCachePath().isEmpty()) {
            initConfig.put("cache-path", config.getCachePath());
        }

        initConfig.put("allow-unsigned-extensions", true);

        return initConfig;
    }

    @PreDestroy
    public void shutdown() {
        initLock.lock();
        try {
            if (initialized) {
                log.info("Shutting down TrexSQL instance");
                try {
                    Trexsql.shutdown();
                    log.info("TrexSQL instance shut down successfully");
                } catch (Exception e) {
                    log.error("Error shutting down TrexSQL instance: {}", e.getMessage(), e);
                } finally {
                    initialized = false;
                }
            }
        } finally {
            initLock.unlock();
        }
    }
}
