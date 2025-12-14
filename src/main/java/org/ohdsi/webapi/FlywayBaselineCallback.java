package org.ohdsi.webapi;

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Flyway callback that handles baseline migration logic for fresh databases.
 *
 * For fresh databases (empty schema_version), this marks all V2.x migrations
 * as applied so that only V3.0.0.0 baseline and later migrations run.
 *
 * For existing databases, this does nothing - the V2.15.0.20251214000000 migration
 * handles marking the V3.0.0.0 baseline as applied.
 */
public class FlywayBaselineCallback implements Callback {

    private static final Logger log = LoggerFactory.getLogger(FlywayBaselineCallback.class);

    // Version threshold - migrations below this are skipped for fresh installs
    private static final String BASELINE_VERSION = "3.0.0.0";

    @Override
    public boolean supports(Event event, Context context) {
        return event == Event.BEFORE_MIGRATE;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }

    @Override
    public void handle(Event event, Context context) {
        if (event != Event.BEFORE_MIGRATE) {
            return;
        }

        try {
            // Don't close the connection - Flyway manages it
            Connection conn = context.getConnection();
            if (isFreshDatabase(conn, context)) {
                log.info("Fresh database detected - marking V2.x migrations as applied to use V3.0.0.0 baseline");
                markOldMigrationsAsApplied(conn, context);
            }
        } catch (SQLException e) {
            log.warn("Error checking database state for baseline: {}", e.getMessage());
        }
    }

    private boolean isFreshDatabase(Connection conn, Context context) throws SQLException {
        String schema = context.getConfiguration().getDefaultSchema();
        String table = context.getConfiguration().getTable();
        String fullTableName = schema != null ? schema + "." + table : table;

        // Check if schema_version table exists and has any entries
        String sql = "SELECT COUNT(*) FROM " + fullTableName;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            // Table doesn't exist yet - this is a fresh database
            return true;
        }
        return false;
    }

    private void markOldMigrationsAsApplied(Connection conn, Context context) throws SQLException {
        String schema = context.getConfiguration().getDefaultSchema();
        String table = context.getConfiguration().getTable();
        String fullTableName = schema != null ? schema + "." + table : table;

        // Ensure schema_version table exists
        try {
            conn.prepareStatement("SELECT 1 FROM " + fullTableName + " LIMIT 1").executeQuery();
        } catch (SQLException e) {
            // Table doesn't exist - Flyway will create it, we'll handle this in afterMigrate if needed
            log.debug("Schema version table doesn't exist yet, Flyway will create it");
            return;
        }

        // Insert a baseline marker that tells Flyway to skip V2.x migrations
        // We insert a synthetic "baseline" entry at version 2.15.0.20251214000000
        String insertSql = "INSERT INTO " + fullTableName +
            " (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) " +
            "SELECT COALESCE(MAX(installed_rank), 0) + 1, '2.15.0.20251214000000', " +
            "'<< Flyway Baseline for V3.0.0.0 >>', 'BASELINE', '<< Baseline >>', NULL, 'WebAPI', NOW(), 0, true " +
            "FROM " + fullTableName + " " +
            "WHERE NOT EXISTS (SELECT 1 FROM " + fullTableName + " WHERE type = 'BASELINE')";

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                log.info("Inserted baseline marker at version 2.15.0.20251214000000 - V2.x migrations will be skipped");
            }
        }
    }

    @Override
    public String getCallbackName() {
        return "BaselineCallback";
    }
}
