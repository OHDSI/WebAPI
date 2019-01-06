package org.ohdsi.webapi;

import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.ohdsi.webapi.source.NotEncrypted;
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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 *
 */
@Configuration
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
        properties.setProperty("hibernate.default_schema", this.env.getProperty("spring.jpa.properties.hibernate.default_schema"));
        properties.setProperty("hibernate.dialect", this.env.getProperty("spring.jpa.properties.hibernate.dialect"));
        properties.setProperty("hibernate.id.new_generator_mappings", "false");
        return properties;
    }
      
    @Bean
		@DependsOn("defaultStringEncryptor")
    @Primary    
    public DataSource primaryDataSource() {
        String driver = this.env.getRequiredProperty("datasource.driverClassName");
        String url = this.env.getRequiredProperty("datasource.url");
        String user = this.env.getRequiredProperty("datasource.username");
        String pass = this.env.getRequiredProperty("datasource.password");
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
        //note autocommit defaults vary across vendors. use provided @Autowired TransactionTemplate

        String[] supportedDrivers;
        supportedDrivers = new String[]{"org.postgresql.Driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "oracle.jdbc.driver.OracleDriver", "com.amazon.redshift.jdbc.Driver", "com.cloudera.impala.jdbc41.Driver", "net.starschema.clouddb.jdbc.BQDriver", "org.netezza.Driver", "com.simba.googlebigquery.jdbc42.Driver"};
        for (String driverName : supportedDrivers) {
            try {
                Class.forName(driverName);
                logger.info("driver loaded: {}", driverName);
            } catch (Exception ex) {
                logger.info("error loading {} driver. {}", driverName, ex.getMessage());
            }
        }
        return ds;
        //return new org.apache.tomcat.jdbc.pool.DataSource(pc);
    }

    @Bean
    public PBEStringEncryptor defaultStringEncryptor(){

        PBEStringEncryptor stringEncryptor;
        if (encryptorEnabled) {
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setProvider(new BouncyCastleProvider());
            encryptor.setProviderName("BC");
            encryptor.setAlgorithm(env.getRequiredProperty("jasypt.encryptor.algorithm"));
						if ("PBEWithMD5AndDES".equals(env.getRequiredProperty("jasypt.encryptor.algorithm"))) {
							logger.warn("Warning:  encryption algorithm set to PBEWithMD5AndDES, which is not considered a strong encryption algorithm.  You may use PBEWITHSHA256AND128BITAES-CBC-BC, but will require special JVM configuration to support these stronger methods.");
						}
            encryptor.setKeyObtentionIterations(1000);
            String password = env.getRequiredProperty("jasypt.encryptor.password");
            if (StringUtils.isNotEmpty(password)) {
                encryptor.setPassword(password);
            }
            stringEncryptor = encryptor;
        } else {
            stringEncryptor = new NotEncrypted();
        }
        HibernatePBEEncryptorRegistry.getInstance()
                .registerPBEStringEncryptor("defaultStringEncryptor", stringEncryptor);
        return stringEncryptor;
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(Boolean.valueOf(this.env.getRequiredProperty("spring.jpa.show-sql")));
        //hibernate.dialect is resolved based on driver
        //vendorAdapter.setDatabasePlatform(hibernateDialect);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setJpaProperties(getJPAProperties());
        factory.setPackagesToScan("org.ohdsi.webapi");
        factory.setDataSource(primaryDataSource());
        factory.afterPropertiesSet();

        return factory.getObject();
    }

    @Bean
    @Primary
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
