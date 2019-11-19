package org.ohdsi.webapi.migration;

import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.testcontainers.containers.MSSQLServerContainer;

import java.sql.ResultSet;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MSSQLServerMigrationsTest extends AbstractContainerDatabaseTest {
    public static MSSQLServerContainer mssqlserver = new MSSQLServerContainer();

    @BeforeClass
    public static void smokeTestMigrations() throws Exception {
        mssqlserver.start();
    }

    @Test
    public void smokeTestMSSQLServerMigrations() {
        Flyway flyway = new Flyway();

        flyway.setDataSource(mssqlserver.getJdbcUrl(), mssqlserver.getUsername(), mssqlserver.getPassword());
        flyway.setSchemas("test");
        flyway.setPlaceholders(new HashMap<String, String>() {{
            put("ohdsiSchema", "test");
        }});

        // FIXME: Java migrations are not currently tested here because
        // they don't implement the interfaces that Flyway uses to
        // auto detect them
        flyway.setLocations("classpath:org/ohdsi/webapi/db/migartion", "classpath:db/migration/sqlserver");

        // TODO: Add test data?
        flyway.migrate();
    }

    @Test
    public void testV2_5_0_20191118124900__fix_mode_id_missing() throws Exception {
        String query;
        ResultSet resultSet;

        // Test cohort_inclusion_result table
        query = String.format("SELECT column_name FROM information_schema.columns WHERE table_schema='%s'" +
            "and table_name='%s' and column_name='%s';", "test", "cohort_inclusion_result", "mode_id");
        resultSet = performQuery(mssqlserver, query);
        assertEquals(resultSet.getString("column_name"), "mode_id");

        // Test cohort_inclusion_stats table
        query = String.format("SELECT column_name FROM information_schema.columns WHERE table_schema='%s'" +
            "and table_name='%s' and column_name='%s';", "test", "cohort_inclusion_stats", "mode_id");
        resultSet = performQuery(mssqlserver, query);
        assertEquals(resultSet.getString("column_name"), "mode_id");

        // Test cohort_summary_stats table
        query = String.format("SELECT column_name FROM information_schema.columns WHERE table_schema='%s'" +
            "and table_name='%s' and column_name='%s';", "test", "cohort_summary_stats", "mode_id");
        resultSet = performQuery(mssqlserver, query);
        assertEquals(resultSet.getString("column_name"), "mode_id");
    }
}
