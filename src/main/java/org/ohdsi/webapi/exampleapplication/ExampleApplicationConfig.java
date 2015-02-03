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
    public Job batchJob() {
        return this.jobBuilders.get("batchJob").start(step()).build();
    }
    
    @Bean
    public Step step() {
        return this.stepBuilders.get("step").tasklet(tasklet()).allowStartIfComplete(true)/*.taskExecutor(this.taskExecutor)*/
                .build();
    }
    
    @Bean
    public Tasklet tasklet() {
        return new Tasklet() {
            
            @Override
            public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext)
                                                                                                             throws Exception {
                log.info("Tasklet execution >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                return RepeatStatus.FINISHED;
            }
        };
        
    }
}
