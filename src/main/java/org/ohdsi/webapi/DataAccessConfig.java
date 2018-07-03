package org.ohdsi.webapi;

import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.jnj.honeur.webapi.CurrentTenantIdentifierResolverImpl;
import com.jnj.honeur.webapi.DataSourceLookup;
import com.jnj.honeur.webapi.MultiTenantConnectionProviderImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MultiTenancyStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
public class DataAccessConfig {

    private static final Log log = LogFactory.getLog(DataAccessConfig.class);

    @Autowired
    private Environment env;
  
    private Properties getJPAProperties() {
        Properties properties = new Properties();
        properties.setProperty(org.hibernate.cfg.Environment.DEFAULT_SCHEMA, this.env.getProperty("spring.jpa.properties.hibernate.default_schema"));
        properties.setProperty(org.hibernate.cfg.Environment.DIALECT, this.env.getProperty("spring.jpa.properties.hibernate.dialect"));
        properties.setProperty(org.hibernate.cfg.Environment.SHOW_SQL, env.getProperty("spring.jpa.show-sql"));
        properties.setProperty(org.hibernate.cfg.Environment.USE_NEW_ID_GENERATOR_MAPPINGS, "false");
        return properties;
    }
      
    @Bean
    @Primary    
    public DataSource primaryDataSource() {
        String driver = this.env.getRequiredProperty("datasource.driverClassName");
        String url = this.env.getRequiredProperty("datasource.url");
        String user = this.env.getRequiredProperty("datasource.username");
        String pass = this.env.getRequiredProperty("datasource.password");
        String schema = this.env.getRequiredProperty("datasource.ohdsi.schema");

        boolean autoCommit = false;


        //pooling - currently issues with (at least) oracle with use of temp tables and "on commit preserve rows" instead of "on commit delete rows";
        //http://forums.ohdsi.org/t/transaction-vs-session-scope-for-global-temp-tables-statements/333/2
        /*final PoolConfiguration pc = new org.apache.tomcat.jdbc.pool.PoolProperties();
     pc.setDriverClassName(driver);
     pc.setUrl(url);
     pc.setUsername(user);
     pc.setPassword(pass);
     pc.setDefaultAutoCommit(autoCommit);*/
        //non-pooling
        DriverManagerDataSource ds = new DriverManagerDataSource(url, user, pass);
        ds.setDriverClassName(driver);
        ds.setSchema(schema);
        //note autocommit defaults vary across vendors. use provided @Autowired TransactionTemplate

        String[] supportedDrivers;
        supportedDrivers = new String[]{"org.postgresql.Driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "oracle.jdbc.driver.OracleDriver", "com.amazon.redshift.jdbc.Driver", "com.cloudera.impala.jdbc4.Driver", "net.starschema.clouddb.jdbc.BQDriver", "org.netezza.Driver"};
        for (String driverName : supportedDrivers) {
            try {
                Class.forName(driverName);
                System.out.println("driver loaded: " + driverName);
            } catch (Exception ex) {
                System.out.println("error loading " + driverName + " driver.");
            }
        }
        return ds;
        //return new org.apache.tomcat.jdbc.pool.DataSource(pc);
    }

    @Bean
    DataSourceLookup dataSourceLookup() {
        return new DataSourceLookup();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        final DataSourceLookup dataSourceLookup = dataSourceLookup();

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(Boolean.valueOf(this.env.getRequiredProperty("spring.jpa.show-sql")));
        //hibernate.dialect is resolved based on driver
        //vendorAdapter.setDatabasePlatform(hibernateDialect);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setJpaProperties(getJPAProperties());
        factory.setPackagesToScan("com.jnj.honeur.webapi", DataAccessConfig.class.getPackage().getName());  // "org.ohdsi.webapi"
        factory.setDataSource(primaryDataSource());
        factory.getJpaPropertyMap().put(org.hibernate.cfg.Environment.MULTI_TENANT, MultiTenancyStrategy.DATABASE);
        factory.getJpaPropertyMap().put(org.hibernate.cfg.Environment.MULTI_TENANT_CONNECTION_PROVIDER, new MultiTenantConnectionProviderImpl(dataSourceLookup));
        factory.getJpaPropertyMap().put(org.hibernate.cfg.Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, new CurrentTenantIdentifierResolverImpl());
        factory.afterPropertiesSet();

        return factory.getObject();
    }

    @Bean
    @Primary
    @Order(Integer.MIN_VALUE+1)
    //This is needed so that JpaTransactionManager is used for autowiring, instead of DataSourceTransactionManager
    public PlatformTransactionManager jpaTransactionManager() {//EntityManagerFactory entityManagerFactory) {

        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory());
        return txManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager);
        return transactionTemplate;
    }

    @Bean
    public TransactionTemplate transactionTemplateRequiresNew(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }
		
    @Bean
    public TransactionTemplate transactionTemplateNoTransaction(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return transactionTemplate;
    }		
  
  /*
  public String getSparqlEndpoint()
  {
	  String sparqlEndpoint = this.env.getRequiredProperty("sparql.endpoint");
	  return sparqlEndpoint;
  }*/
}
