package org.ohdsi.webapi;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.audittrail.listeners.AuditTrailJobListener;
import org.ohdsi.webapi.common.generation.AutoremoveJobListener;
import org.ohdsi.webapi.common.generation.CancelJobListener;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.util.ManagedThreadPoolTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.admin.service.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Spring Batch 5.x configuration for Java 21 / Spring Boot 3.2
 * JobRepository, PlatformTransactionManager, and JobExplorer are auto-configured by Spring Boot
 */
@Configuration
@EnableBatchProcessing
@DependsOn({"batchDatabaseInitializer"})
public class JobConfig {
    
    private static final Logger log = LoggerFactory.getLogger(JobConfig.class);
    
    @Value("${spring.batch.repository.tableprefix}")
    private String tablePrefix;
    
    @Value("${spring.batch.taskExecutor.corePoolSize}")
    private int corePoolSize;
    
    @Value("${spring.batch.taskExecutor.maxPoolSize}")
    private int maxPoolSize;
    
    @Value("${spring.batch.taskExecutor.queueCapacity}")
    private int queueCapacity;
    
    @Value("${spring.batch.repository.isolationLevelForCreate}")
    private String isolationLevelForCreate;
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JobService jobService;
    
    @Autowired
    private Security security;
    
    @Autowired
    private AuditTrailJobListener auditTrailJobListener;

    @PostConstruct
    private void init() {
        if (StringUtils.isEmpty(this.tablePrefix)) {
            throw new RuntimeException("Batch table prefix cannot be empty");
        }
        log.info("Batch table prefix: {}", this.tablePrefix);
    }
    
    @Bean
    public TaskExecutor batchTaskExecutor() {
        ManagedThreadPoolTaskExecutor taskExecutor = new ManagedThreadPoolTaskExecutor(jobService, security);
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(2);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }
    
    // Spring Batch 5: JobRepository is auto-configured, we just inject it
    // Custom JobLauncher for async execution
    @Bean
    @Primary
    public JobLauncher asyncJobLauncher(JobRepository jobRepository, TaskExecutor batchTaskExecutor) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(batchTaskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
    
    @Bean
    public JobTemplate jobTemplate(JobLauncher jobLauncher, JobRepository jobRepository, Security security) {
        // Spring Batch 5: JobBuilderFactory removed, pass JobRepository directly
        return new JobTemplate(jobLauncher, jobRepository, security);
    }
    
    @Bean
    public SearchableJobExecutionDao searchableJobExecutionDao(DataSource dataSource) {
        JdbcSearchableJobExecutionDao dao = new JdbcSearchableJobExecutionDao();
        dao.setDataSource(dataSource);
        dao.setTablePrefix(this.tablePrefix); 
        return dao;
    }
    
    @Bean
    public SearchableJobInstanceDao searchableJobInstanceDao(JdbcTemplate jdbcTemplate) {
        JdbcSearchableJobInstanceDao dao = new JdbcSearchableJobInstanceDao();
        dao.setJdbcTemplate(jdbcTemplate);
        dao.setTablePrefix(this.tablePrefix); 
        return dao;
    }
}
