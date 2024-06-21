package org.ohdsi.webapi;

import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.audittrail.listeners.AuditTrailJobListener;
import org.ohdsi.webapi.common.generation.AutoremoveJobListener;
import org.ohdsi.webapi.common.generation.CancelJobListener;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.util.ManagedThreadPoolTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.admin.service.JdbcSearchableJobExecutionDao;
import org.springframework.batch.admin.service.JdbcSearchableJobInstanceDao;
import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.admin.service.SearchableJobInstanceDao;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
//@DependsOn({"batchDatabaseInitializer"})
public class JobConfig extends DefaultBatchConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JobConfig.class);

    @Value("${spring.batch.repository.tableprefix}")
    private String tablePrefix;

    @Value("${spring.batch.repository.isolationLevelForCreate}")
    private String isolationLevelForCreate;

    @Value("${spring.batch.taskExecutor.corePoolSize}")
    private Integer corePoolSize;

    @Value("${spring.batch.taskExecutor.maxPoolSize}")
    private Integer maxPoolSize;

    @Value("${spring.batch.taskExecutor.queueCapacity}")
    private Integer queueCapacity;

    @Value("${spring.batch.taskExecutor.threadGroupName}")
    private String threadGroupName;

    @Value("${spring.batch.taskExecutor.threadNamePrefix}")
    private String threadNamePrefix;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuditTrailJobListener auditTrailJobListener;

    @Bean
    String batchTablePrefix() {
        return this.tablePrefix;
    }

    @Bean
    TaskExecutor taskExecutor() {
        final ThreadPoolTaskExecutor taskExecutor = new ManagedThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        if (StringUtils.isNotBlank(threadGroupName)) {
            taskExecutor.setThreadGroupName(threadGroupName);
        }
        if (StringUtils.isNotBlank(threadNamePrefix)) {
            taskExecutor.setThreadNamePrefix(threadNamePrefix);
        }
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public JobRepository jobRepository() {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setIsolationLevelForCreate(isolationLevelForCreate);
        factory.setTablePrefix(tablePrefix);
        factory.setTransactionManager(new ResourcelessTransactionManager());
        factory.setValidateTransactionState(false);
        try {
			factory.afterPropertiesSet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			return factory.getObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

    @Bean
    public JobLauncher jobLauncher() {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository());
        jobLauncher.setTaskExecutor(taskExecutor());
        try {
			jobLauncher.afterPropertiesSet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return jobLauncher;
    }

    @Bean
    public JobExplorer jobExplorer() {
        JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
        jobExplorerFactoryBean.setDataSource(dataSource);
        jobExplorerFactoryBean.setTablePrefix(tablePrefix);
        jobExplorerFactoryBean.setTransactionManager(getTransactionManager());
        try {
			jobExplorerFactoryBean.afterPropertiesSet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			return jobExplorerFactoryBean.getObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

    @Bean
    JobTemplate jobTemplate(final JobLauncher jobLauncher, final JobBuilderFactory jobBuilders,
                            final StepBuilderFactory stepBuilders, final Security security) {
        return new JobTemplate(jobLauncher, jobBuilders, stepBuilders, security);
    }

    @Bean
    SearchableJobExecutionDao searchableJobExecutionDao(DataSource dataSource) {
        JdbcSearchableJobExecutionDao dao = new JdbcSearchableJobExecutionDao();
        dao.setDataSource(dataSource);
        dao.setTablePrefix(this.tablePrefix);
        return dao;
    }

    @Bean
    SearchableJobInstanceDao searchableJobInstanceDao(JdbcTemplate jdbcTemplate) {
        JdbcSearchableJobInstanceDao dao = new JdbcSearchableJobInstanceDao();
        dao.setJdbcTemplate(jdbcTemplate);
        dao.setTablePrefix(this.tablePrefix);
        return dao;
    }


    @Primary
    @Bean
    JobBuilderFactory jobBuilders(JobRepository jobRepository) {
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
    StepBuilderFactory stepBuilders(JobRepository jobRepository) {
        return new StepBuilderFactory(jobRepository);
    }
}