package org.ohdsi.webapi;

import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "/application-test.properties")
public abstract class AbstractDatabaseTest {

  static class JdbcTemplateTestWrapper extends ExternalResource {

    @Override
    protected void before() throws Throwable {
      jdbcTemplate = new JdbcTemplate(getDataSource());
      try {
        // note for future reference: should probably either define a TestContext DataSource with these params
        // or make it so this proparty is only set once (during database initialization) since the below will run for each test class (but only be effective once)
        System.setProperty("datasource.url", getDataSource().getConnection().getMetaData().getURL());
        System.setProperty("flyway.datasource.url", System.getProperty("datasource.url"));
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  static class DriverExcludeTestWrapper extends ExternalResource {

    @Override
    protected void before() throws Throwable {
      // Put the redshift driver at the end so that it doesn't
      // conflict with postgres queries
      java.util.Enumeration<Driver> drivers = DriverManager.getDrivers();
      while (drivers.hasMoreElements()) {
        Driver driver = drivers.nextElement();
        if (driver.getClass().getName().contains("com.amazon.redshift.jdbc")) {
          try {
            DriverManager.deregisterDriver(driver);
            DriverManager.registerDriver(driver);
          } catch (SQLException e) {
            throw new RuntimeException("Could not deregister redshift driver", e);
          }
        }
      }
    }
  }

  @ClassRule
  public static TestRule chain = RuleChain.outerRule(new DriverExcludeTestWrapper())
          .around(pg = new PostgresSingletonRule())
          .around(new JdbcTemplateTestWrapper());

  protected static PostgresSingletonRule pg;

  protected static JdbcTemplate jdbcTemplate;

  protected static DataSource getDataSource() {
    return pg.getEmbeddedPostgres().getPostgresDatabase();
  }

  protected void truncateTable(String tableName) {
    jdbcTemplate.execute(String.format("TRUNCATE %s CASCADE", tableName));
  }

  protected void resetSequence(String sequenceName) {
    jdbcTemplate.execute(String.format("ALTER SEQUENCE %s RESTART WITH 1", sequenceName));
  }

  protected static IDatabaseConnection getConnection() throws SQLException {
    final IDatabaseConnection con = new DatabaseDataSourceConnection(getDataSource());
    con.getConfig().setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
    con.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
    return con;
  }
  
  protected void loadPrepData(String[] datasetPaths) throws Exception {
    loadPrepData(datasetPaths, DatabaseOperation.CLEAN_INSERT);
  }
  protected void loadPrepData(String[] datasetPaths, DatabaseOperation dbOp) throws Exception {
    final IDatabaseConnection dbUnitCon = getConnection();
    final IDataSet ds = DataSetFactory.createDataSet(datasetPaths);

    assertNotNull("No dataset found", ds);

    try {
      dbOp.execute(dbUnitCon, ds); // clean load of the DB. Careful, clean means "delete the old stuff"
    } catch (final DatabaseUnitException e) {
      fail(e.getMessage());
    } finally {
      dbUnitCon.close();
    }
  }  
}
