/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package org.ohdsi.webapi;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.explore.support.MapJobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * https://github.com/spring-projects/spring-boot/issues/1655
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    private static final Log log = LogFactory.getLog(CustomBatchConfigurer.class);
    
    @Value("${spring.batch.repository.tableprefix}")
    private String tablePrefix;
    
    @Autowired
    private DataSource dataSource;
    
    @Bean
    public String batchTablePrefix() {
        return this.tablePrefix;
    }
    
    @Bean
    public TaskExecutor taskExecutor() {
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(10);//TODO
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }
    
    @Bean
    public BatchConfigurer batchConfigurer() {
        return new CustomBatchConfigurer(this.dataSource);
    }
    
    class CustomBatchConfigurer implements BatchConfigurer {
        
        private DataSource dataSource;
        
        private PlatformTransactionManager transactionManager;
        
        private JobRepository jobRepository;
        
        private JobLauncher jobLauncher;
        
        private JobExplorer jobExplorer;
        
        @Autowired(required = false)
        public void setDataSource(final DataSource dataSource) {
            this.dataSource = dataSource;
            this.transactionManager = new DataSourceTransactionManager(dataSource);
        }
        
        protected CustomBatchConfigurer() {
        }
        
        public CustomBatchConfigurer(final DataSource dataSource) {
            setDataSource(dataSource);
        }
        
        @Override
        public JobRepository getJobRepository() {
            return this.jobRepository;
        }
        
        @Override
        public PlatformTransactionManager getTransactionManager() {
            return this.transactionManager;
        }
        
        @Override
        public JobLauncher getJobLauncher() {
            return this.jobLauncher;
        }
        
        @Override
        public JobExplorer getJobExplorer() {
            return this.jobExplorer;
        }
        
        @PostConstruct
        public void initialize() {
            try {
                if (this.dataSource == null) {
                    log.warn("No datasource was provided...using a Map based JobRepository");
                    
                    if (this.transactionManager == null) {
                        this.transactionManager = new ResourcelessTransactionManager();
                    }
                    
                    final MapJobRepositoryFactoryBean jobRepositoryFactory = new MapJobRepositoryFactoryBean(
                            this.transactionManager);
                    jobRepositoryFactory.afterPropertiesSet();
                    this.jobRepository = jobRepositoryFactory.getObject();
                    
                    final MapJobExplorerFactoryBean jobExplorerFactory = new MapJobExplorerFactoryBean(jobRepositoryFactory);
                    jobExplorerFactory.afterPropertiesSet();
                    this.jobExplorer = jobExplorerFactory.getObject();
                } else {
                    this.jobRepository = createJobRepository();
                    
                    final JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
                    jobExplorerFactoryBean.setDataSource(this.dataSource);
                    jobExplorerFactoryBean.afterPropertiesSet();
                    this.jobExplorer = jobExplorerFactoryBean.getObject();
                }
                
                this.jobLauncher = createJobLauncher();
            } catch (final Exception e) {
                throw new BatchConfigurationException(e);
            }
        }
        
        private JobLauncher createJobLauncher() throws Exception {
            final SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
            //async TODO
            jobLauncher.setTaskExecutor(taskExecutor());
            jobLauncher.setJobRepository(this.jobRepository);
            jobLauncher.afterPropertiesSet();
            return jobLauncher;
        }
        
        protected JobRepository createJobRepository() throws Exception {
            final JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
            factory.setDataSource(this.dataSource);
            //prevent ISOLATION_DEFAULT setting for oracle (i.e. SERIALIZABLE)
            factory.setTablePrefix(BatchConfig.this.tablePrefix);
            factory.setTransactionManager(getTransactionManager());
            factory.afterPropertiesSet();
            return factory.getObject();
        }
    }
}

/*extends DefaultBatchConfigurer {
    
    private static final Log log = LogFactory.getLog(BatchConfig.class);
    
    @Autowired
    private JobBuilderFactory jobBuilders;
    
    @Autowired
    private StepBuilderFactory stepBuilders;
    
    @Autowired
    private DataSource dataSource;
    
    @Value("${spring.batch.repository.tableprefix}")
    private String tablePrefix;
    
    @Bean
    public String batchTablePrefix() {
        return this.tablePrefix;
    }
    
    @Override
    protected JobRepository createJobRepository() throws Exception {
        final JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(this.dataSource);
        factory.setTablePrefix(this.tablePrefix);
        factory.setIsolationLevelForCreate(this.isolationLevel);
        factory.setTransactionManager(getTransactionManager());
        factory.afterPropertiesSet();
        return factory.getObject();
    }
    
    @Bean
    public TaskExecutor taskExecutor() {
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(2);//TODO
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }
}
*/
