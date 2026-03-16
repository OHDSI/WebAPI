package org.ohdsi.webapi;

import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean;
import org.ohdsi.webapi.arachne.encryption.EncryptorUtils;
import org.ohdsi.webapi.arachne.encryption.NotEncrypted;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 */
@Configuration
@Lazy(false)
@EnableTransactionManagement
@EnableJpaRepositories(repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean.class)
public class DataAccessConfig {

    private final Logger logger = LoggerFactory.getLogger(DataAccessConfig.class);
	
    @Autowired
    private Environment env;
    @Value("${jasypt.encryptor.enabled}")
    private boolean encryptorEnabled;
  
    private Properties getJPAProperties() {
        Properties properties = new Properties();
        // Only set optional Hibernate properties when present to avoid null values
        putIfPresent(properties, "hibernate.default_schema", this.env.getProperty("spring.jpa.properties.hibernate.default_schema"));
        putIfPresent(properties, "hibernate.dialect", this.env.getProperty("spring.jpa.properties.hibernate.dialect"));
        putIfPresent(properties, "hibernate.generate_statistics", this.env.getProperty("spring.jpa.properties.hibernate.generate_statistics"));
        putIfPresent(properties, "hibernate.jdbc.batch_size", this.env.getProperty("spring.jpa.properties.hibernate.jdbc.batch_size"));
        putIfPresent(properties, "hibernate.order_inserts", this.env.getProperty("spring.jpa.properties.hibernate.order_inserts"));
        properties.setProperty("hibernate.id.new_generator_mappings", "true");
        return properties;
    }

    private static void putIfPresent(Properties target, String key, String value) {
        if (value != null) {
            target.setProperty(key, value);
        }
    }
      
    @Bean({"primaryDataSource", "dataSource"})
	@DependsOn("defaultStringEncryptor")
    @Primary    
    public DataSource primaryDataSource() {
        logger.info("datasource.url is: " + this.env.getRequiredProperty("datasource.url"));
        String driver = this.env.getRequiredProperty("datasource.driverClassName");
        String url = this.env.getRequiredProperty("datasource.url");
        String user = this.env.getRequiredProperty("datasource.username");
        String pass = this.env.getRequiredProperty("datasource.password");

        // Use HikariCP for connection pooling
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driver);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(pass);
        
        // Apply Hikari configuration from properties
        hikariConfig.setMaximumPoolSize(env.getProperty("spring.datasource.hikari.maximum-pool-size", Integer.class, 10));
        hikariConfig.setMinimumIdle(env.getProperty("spring.datasource.hikari.minimum-idle", Integer.class, 0));
        hikariConfig.setConnectionTimeout(env.getProperty("spring.datasource.hikari.connection-timeout", Long.class, 30000L));
        hikariConfig.setConnectionTestQuery(env.getProperty("spring.datasource.hikari.connection-test-query", "SELECT 1"));
        hikariConfig.setAutoCommit(false);
        
        HikariDataSource ds = new HikariDataSource(hikariConfig);

        String[] supportedDrivers;
        supportedDrivers = new String[]{"org.postgresql.Driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "oracle.jdbc.driver.OracleDriver", "com.amazon.redshift.jdbc.Driver", "com.cloudera.impala.jdbc.Driver", "net.starschema.clouddb.jdbc.BQDriver", "org.netezza.Driver", "com.simba.googlebigquery.jdbc42.Driver", "org.apache.hive.jdbc.HiveDriver", "com.simba.spark.jdbc.Driver", "net.snowflake.client.jdbc.SnowflakeDriver", "com.databricks.client.jdbc.Driver", "com.intersystems.jdbc.IRISDriver"};
        for (String driverName : supportedDrivers) {
            try {
                Class.forName(driverName);
                logger.info("driver loaded: {}", driverName);
            } catch (Exception ex) {
                logger.info("error loading {} driver. {}", driverName, ex.getMessage());
            }
        }

        // Redshift driver can be loaded first because it is mentioned in manifest file -
        // put the redshift driver at the end so that it doesn't
        // conflict with postgres queries
        java.util.Enumeration<Driver> drivers =  DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver d = drivers.nextElement();
            if (d.getClass().getName().contains("com.amazon.redshift.jdbc")) {
                try {
                    DriverManager.deregisterDriver(d);
                    DriverManager.registerDriver(d);
                } catch (SQLException e) {
                    throw new RuntimeException("Could not deregister redshift driver", e);
                }
            }
        }

        return ds;
    }

    @Bean
    public PBEStringEncryptor defaultStringEncryptor(){

        PBEStringEncryptor stringEncryptor = encryptorEnabled ?
                EncryptorUtils.buildStringEncryptor(env) :
                new NotEncrypted();

        return stringEncryptor;
    }

    @Bean
    public EntityManagerFactory entityManagerFactory(DataSource dataSource,
            ConfigurableListableBeanFactory beanFactory) {

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(Boolean.valueOf(this.env.getRequiredProperty("spring.jpa.show-sql")));
        //hibernate.dialect is resolved based on driver
        //vendorAdapter.setDatabasePlatform(hibernateDialect);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);

        Properties props = getJPAProperties();
        // Spring/Hibernate integration: register SpringBeanContainer so that Hibernate
        // resolves @Converter and other managed beans from the Spring ApplicationContext
        // instead of instantiating them via reflection. This allows @Autowired injection
        // to work on EncryptedStringConverter (and any other JPA AttributeConverters),
        // which is how the Jasypt PBEStringEncryptor gets wired into the converter.
        // Without this, Hibernate calls new EncryptedStringConverter() directly and the
        // encryptor field remains null.
        props.put(org.hibernate.cfg.AvailableSettings.BEAN_CONTAINER, new SpringBeanContainer(beanFactory));
        factory.setJpaProperties(props);

        factory.setPackagesToScan("org.ohdsi.webapi");
        factory.setDataSource(dataSource);
        factory.afterPropertiesSet();

        return factory.getObject();
    }

    @Bean({"jpaTransactionManager", "transactionManager"})
    @Primary
    //This is needed so that JpaTransactionManager is used for autowiring, instead of DataSourceTransactionManager
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {

        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
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
