package org.ohdsi.webapi.migration;

import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.ResultSet;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PostgresMigrationTest extends AbstractContainerDatabaseTest {
    public static PostgreSQLContainer postgres = new PostgreSQLContainer<>();

    @BeforeClass
    public static void smokeTestMigrations() throws Exception {
        postgres.start();
    }

    @Test
    public void smokeTestPostgresMigrations() {
        Flyway flyway = new Flyway();

        flyway.setDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        flyway.setSchemas("test");
        flyway.setPlaceholders(new HashMap<String, String>() {{
            put("ohdsiSchema", "test");
        }});

        // FIXME: Java migrations are not currently tested here because
        // they don't implement the interfaces that Flyway uses to
        // auto detect them
        flyway.setLocations("classpath:org/ohdsi/webapi/db/migartion", "classpath:db/migration/postgresql");

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
        resultSet = performQuery(postgres, query);
        assertEquals(resultSet.getString("column_name"), "mode_id");

        // Test cohort_inclusion_stats table
        query = String.format("SELECT column_name FROM information_schema.columns WHERE table_schema='%s'" +
            "and table_name='%s' and column_name='%s';", "test", "cohort_inclusion_stats", "mode_id");
        resultSet = performQuery(postgres, query);
        assertEquals(resultSet.getString("column_name"), "mode_id");

        // Test cohort_summary_stats table
        query = String.format("SELECT column_name FROM information_schema.columns WHERE table_schema='%s'" +
            "and table_name='%s' and column_name='%s';", "test", "cohort_summary_stats", "mode_id");
        resultSet = performQuery(postgres, query);
        assertEquals(resultSet.getString("column_name"), "mode_id");
    }
}
