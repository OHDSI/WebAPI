package org.ohdsi.webapi.migration;

import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.testcontainers.containers.OracleContainer;

import java.sql.ResultSet;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OracleMigrationTest extends AbstractContainerDatabaseTest {
    public static OracleContainer oracle = new OracleContainer("wnameless/oracle-xe-11g-r2");

    @BeforeClass
    public static void smokeTestMigrations() throws Exception {
        oracle.withInitScript("migration/oracle-setup.sql");
        oracle.start();
    }

    @Test
    public void smokeTestOracleMigrations() {
        Flyway flyway = new Flyway();

        flyway.setDataSource(String.format("jdbc:oracle:thin:@localhost:%s:xe", oracle.getOraclePort()), "test", "test");
        flyway.setPlaceholders(new HashMap<String, String>() {{
            put("ohdsiSchema", "test");
        }});

        // FIXME: Java migrations are not currently tested here because
        // they don't implement the interfaces that Flyway uses to
        // auto detect them
        flyway.setLocations("classpath:org/ohdsi/webapi/db/migartion", "classpath:db/migration/oracle");

        // TODO: Add test data?
        flyway.migrate();
    }

    @Test
    public void testV2_5_0_20191118124900__fix_mode_id_missing() throws Exception {
        String query;
        ResultSet resultSet;

        // Test cohort_inclusion_result table
        query = String.format("SELECT column_name FROM all_tab_cols WHERE owner='%s' and table_name='%s' and " +
            "column_name='%s'", "TEST", "COHORT_INCLUSION", "MODE_ID");
        resultSet = performQuery(oracle, query);
        assertEquals(resultSet.getString("COLUMN_NAME"), "MODE_ID");

        // Test cohort_inclusion_stats table
        query = String.format("SELECT column_name FROM all_tab_cols WHERE owner='%s' and table_name='%s' and " +
            "column_name='%s'", "TEST", "COHORT_INCLUSION_RESULT", "MODE_ID");
        resultSet = performQuery(oracle, query);
        assertEquals(resultSet.getString("column_name"), "MODE_ID");

        // Test cohort_summary_stats table
        query = String.format("SELECT column_name FROM all_tab_cols WHERE owner='%s' and table_name='%s' and " +
            "column_name='%s'", "TEST", "COHORT_SUMMARY_STATS", "MODE_ID");
        resultSet = performQuery(oracle, query);
        assertEquals(resultSet.getString("column_name"), "MODE_ID");
    }
}