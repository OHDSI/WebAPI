package org.ohdsi.webapi;

import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean;
import com.odysseusinc.datasourcemanager.encryption.EncryptorUtils;
import com.odysseusinc.datasourcemanager.encryption.NotEncrypted;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
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

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "org.ohdsi.webapi",
    repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean.class
)
public class DataAccessConfig {

    private final Logger logger = LoggerFactory.getLogger(DataAccessConfig.class);

    @Autowired
    private Environment env;

    @Value("${jasypt.encryptor.enabled}")
    private boolean encryptorEnabled;

    private Properties getJPAProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.default_schema", this.env.getProperty("spring.jpa.properties.hibernate.default_schema"));
        properties.setProperty("hibernate.dialect", this.env.getProperty("spring.jpa.properties.hibernate.dialect"));
        properties.setProperty("hibernate.generate_statistics", this.env.getProperty("spring.jpa.properties.hibernate.generate_statistics"));
        properties.setProperty("hibernate.jdbc.batch_size", this.env.getProperty("spring.jpa.properties.hibernate.jdbc.batch_size"));
        properties.setProperty("hibernate.order_inserts", this.env.getProperty("spring.jpa.properties.hibernate.order_inserts"));
        properties.setProperty("hibernate.id.new_generator_mappings", "true");
        return properties;
    }

    @Bean
    @DependsOn("defaultStringEncryptor")
    @Primary
    public DataSource primaryDataSource() {
        logger.info("datasource.url is: " + this.env.getRequiredProperty("datasource.url"));
        String driver = this.env.getRequiredProperty("datasource.driverClassName");
        String url = this.env.getRequiredProperty("datasource.url");
        String user = this.env.getRequiredProperty("datasource.username");
        String pass = this.env.getRequiredProperty("datasource.password");

        DriverManagerDataSource ds = new DriverManagerDataSource(url, user, pass);
        ds.setDriverClassName(driver);

        String[] supportedDrivers = new String[]{
            "org.postgresql.Driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "oracle.jdbc.driver.OracleDriver", 
            "com.amazon.redshift.jdbc.Driver", "com.cloudera.impala.jdbc.Driver", "net.starschema.clouddb.jdbc.BQDriver", 
            "org.netezza.Driver", "com.simba.googlebigquery.jdbc42.Driver", "org.apache.hive.jdbc.HiveDriver", 
            "com.simba.spark.jdbc.Driver", "net.snowflake.client.jdbc.SnowflakeDriver"
        };

        for (String driverName : supportedDrivers) {
            try {
                Class.forName(driverName);
                logger.info("driver loaded: {}", driverName);
            } catch (Exception ex) {
                logger.info("error loading {} driver. {}", driverName, ex.getMessage());
            }
        }

        java.util.Enumeration<Driver> drivers = DriverManager.getDrivers();
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
    public PBEStringEncryptor defaultStringEncryptor() {
        PBEStringEncryptor stringEncryptor = encryptorEnabled ? EncryptorUtils.buildStringEncryptor(env) : new NotEncrypted();

        HibernatePBEEncryptorRegistry.getInstance().registerPBEStringEncryptor("defaultStringEncryptor", stringEncryptor);

        return stringEncryptor;
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(Boolean.parseBoolean(this.env.getRequiredProperty("spring.jpa.show-sql")));

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setJpaProperties(getJPAProperties());
        factory.setPackagesToScan("org.ohdsi.webapi");
        factory.setDataSource(dataSource);
        factory.afterPropertiesSet();

        return factory;
    }

    @Primary
    @Bean
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }

    @Primary
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
}