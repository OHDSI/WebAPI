package org.ohdsi.webapi.exampleapplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

/**
 *
 */
@Configuration
public class ExampleApplicationConfig {
    
    private static final Log log = LogFactory.getLog(ExampleApplicationConfig.class);
    
    @Autowired
    private JobBuilderFactory jobBuilders;
    
    @Autowired
    private StepBuilderFactory stepBuilders;
    
    @Autowired
    private TaskExecutor taskExecutor;
    
    @Bean
    public Job testJob() {
        return this.jobBuilders.get("TestJob").start(testStep()).build();
    }
    
    @Bean
    public Step testStep() {
        return this.stepBuilders.get("TestStep").tasklet(tasklet()).allowStartIfComplete(true).build();
    }
    
    @Bean
    public Tasklet tasklet() {
        return new Tasklet() {
            
            @Override
            public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext)
                                                                                                             throws Exception {
                
                // set variable in JobExecutionContext
                //chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("foo", "bar");
                log.info("Tasklet execution >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                //                Thread.sleep(14000L);
                return RepeatStatus.FINISHED;
            }
        };
        
    }
}
