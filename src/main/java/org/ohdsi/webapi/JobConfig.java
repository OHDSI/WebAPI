package org.ohdsi.webapi;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.audittrail.listeners.AuditTrailJobListener;
import org.ohdsi.webapi.common.generation.AutoremoveJobListener;
import org.ohdsi.webapi.common.generation.CancelJobListener;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.shiro.management.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableBatchProcessing
public class JobConfig {

    private static final Logger log = LoggerFactory.getLogger(JobConfig.class);

    @Value("${spring.batch.repository.tableprefix}")
    private String tablePrefix;

    @Value("${spring.batch.taskExecutor.corePoolSize}")
    private Integer corePoolSize;

    @Value("${spring.batch.taskExecutor.maxPoolSize}")
    private Integer maxPoolSize;

    @Value("${spring.batch.taskExecutor.queueCapacity}")
    private Integer queueCapacity;

    @Value("${spring.batch.taskExecutor.threadNamePrefix}")
    private String threadNamePrefix;

    @Autowired
    private DataSource primaryDataSource;

    @Autowired
    private AuditTrailJobListener auditTrailJobListener;

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setThreadNamePrefix(threadNamePrefix);
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(primaryDataSource);
    }

    @Bean
    public JobRepository jobRepository() {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(primaryDataSource);
        factory.setTransactionManager(transactionManager());
        factory.setTablePrefix(tablePrefix);
        factory.setValidateTransactionState(false);

        try {
            factory.afterPropertiesSet();
            return factory.getObject();
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize JobRepository", e);
        }
    }

    @Bean
    public JobExplorer jobExplorer() throws Exception {
        JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(primaryDataSource); 
        factory.setTransactionManager(transactionManager()); 
        factory.setTablePrefix(tablePrefix); 
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public JobTemplate jobTemplate(JobLauncher jobLauncher, JobBuilderFactory jobBuilders,
                                   StepBuilderFactory stepBuilders, Security security) {
        return new JobTemplate(jobLauncher, jobBuilders, stepBuilders, security);
    }
    
    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository, TaskExecutor taskExecutor) throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(taskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
    
    @Bean
    public JobBuilderFactory jobBuilders(JobRepository jobRepository) {
        return new JobBuilderFactory(jobRepository) {
            @Override
            public JobBuilder get(String name) {
                return super.get(name)
                        .listener(new CancelJobListener())
                        .listener(auditTrailJobListener);
            }
        };
    }

    @Bean
    public StepBuilderFactory stepBuilders(JobRepository jobRepository) {
        return new StepBuilderFactory(jobRepository);
    }
}
