package org.ohdsi.webapi;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
public class DataAccessConfig {
    
    @Autowired
    private Environment env;
    
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "datasource")
    public DataSource primaryDataSource() {
        
        try {
            final PoolConfiguration pc = new org.apache.tomcat.jdbc.pool.PoolProperties();
            pc.setDriverClassName(this.env.getRequiredProperty("datasource.driverClassName"));
            pc.setUrl(this.env.getRequiredProperty("datasource.url"));
            pc.setUsername(this.env.getRequiredProperty("datasource.username"));
            pc.setPassword(this.env.getRequiredProperty("datasource.password"));
            pc.setDefaultAutoCommit(false);
            
            // TODO remaining datasource pool props
            
            return new org.apache.tomcat.jdbc.pool.DataSource(pc);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        
        //        return DataSourceBuilder.create().build();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {

      HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
      vendorAdapter.setGenerateDdl(false);
      vendorAdapter.setShowSql(Boolean.getBoolean(this.env.getRequiredProperty("spring.jpa.show-sql")));

      LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
      factory.setJpaVendorAdapter(vendorAdapter);
      factory.setPackagesToScan("org.ohdsi.webapi");
      factory.setDataSource(primaryDataSource());
      factory.afterPropertiesSet();

      return factory.getObject();
    }
    
    @Bean
    @Primary//This is needed so that JpaTransactionManager is used for autowiring, instead of DataSourceTransactionManager
    public PlatformTransactionManager jpaTransactionManager(){//EntityManagerFactory entityManagerFactory) {

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
}
