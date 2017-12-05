package org.ohdsi.webapi;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.job.JobTemplate;
import org.springframework.batch.admin.service.JdbcSearchableJobExecutionDao;
import org.springframework.batch.admin.service.JdbcSearchableJobInstanceDao;
import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.admin.service.SearchableJobInstanceDao;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Had to copy DefaultBatchConfigurer and include within because jobLauncher config is private.
 * https://github.com/spring-projects/spring-boot/issues/1655
 */
@Configuration
@EnableBatchProcessing
public class JobConfig {
    
    private static final Log log = LogFactory.getLog(CustomBatchConfigurer.class);
    
    @Value("${spring.batch.repository.tableprefix}")
    private String tablePrefix;
    
    @Value("${spring.batch.repository.isolationLevelForCreate}")
    private String isolationLevelForCreate;
    
    @Autowired
    private DataSource dataSource;
    
    @Bean
    public String batchTablePrefix() {
        return this.tablePrefix;
    }
    
    @Bean
    public TaskExecutor taskExecutor() {
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(20);//TODO
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }
    
    @Bean
    public BatchConfigurer batchConfigurer() {
        return new CustomBatchConfigurer(this.dataSource);
    }
    
    @Bean
    public JobTemplate jobTemplate(final JobLauncher jobLauncher, final JobBuilderFactory jobBuilders,
                                   final StepBuilderFactory stepBuilders) {
        return new JobTemplate(jobLauncher, jobBuilders, stepBuilders);
    }
    
    @Bean
    public SearchableJobExecutionDao searchableJobExecutionDao(DataSource dataSource) {
        JdbcSearchableJobExecutionDao dao = new JdbcSearchableJobExecutionDao();
        dao.setDataSource(dataSource);
        dao.setTablePrefix(JobConfig.this.tablePrefix); 
        return dao;
    }
    
    @Bean
    public SearchableJobInstanceDao searchableJobInstanceDao(JdbcTemplate jdbcTemplate) {
        JdbcSearchableJobInstanceDao dao = new JdbcSearchableJobInstanceDao();
        dao.setJdbcTemplate(jdbcTemplate);//no setDataSource as in SearchableJobExecutionDao
        dao.setTablePrefix(JobConfig.this.tablePrefix); 
        return dao;
    }
    
    class CustomBatchConfigurer implements BatchConfigurer {
        
        private DataSource dataSource;
        
        private PlatformTransactionManager transactionManager;
        
        private JobRepository jobRepository;
        
        private JobLauncher jobLauncher;
        
        private JobExplorer jobExplorer;
        
        @Autowired
        public void setDataSource(final DataSource dataSource) {
            this.dataSource = dataSource;
            //            this.transactionManager = new DataSourceTransactionManager(dataSource);
        }
        
        @Autowired
        public void setTransactionManager(final PlatformTransactionManager transactionManager) {
            this.transactionManager = transactionManager;
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
                    jobExplorerFactoryBean.setTablePrefix(JobConfig.this.tablePrefix);
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
            //ISOLATION_REPEATABLE_READ throws READ_COMMITTED and SERIALIZABLE are the only valid transaction levels
            factory.setIsolationLevelForCreate(JobConfig.this.isolationLevelForCreate);
            factory.setTablePrefix(JobConfig.this.tablePrefix);
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
