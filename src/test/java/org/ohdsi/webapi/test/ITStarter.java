package org.ohdsi.webapi.test;

import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.SingleInstancePostgresRule;
import java.sql.SQLException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SecurityIT.class,
        JobServiceIT.class,        
        VocabularyServiceIT.class,
        CohortAnalysisServiceIT.class
})
public class ITStarter {

    @ClassRule
    public static SingleInstancePostgresRule pg = EmbeddedPostgresRules.singleInstance();

    @BeforeClass
    public static void before() {

        try {
            System.setProperty("datasource.url", pg.getEmbeddedPostgres().getPostgresDatabase().getConnection().getMetaData().getURL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.setProperty("flyway.datasource.url", System.getProperty("datasource.url"));
        System.setProperty("security.db.datasource.url", System.getProperty("datasource.url"));
        System.setProperty("security.db.datasource.username", "postgres");
        System.setProperty("security.db.datasource.password", "postgres");
        System.setProperty("security.db.datasource.schema", "public");
    }
}
