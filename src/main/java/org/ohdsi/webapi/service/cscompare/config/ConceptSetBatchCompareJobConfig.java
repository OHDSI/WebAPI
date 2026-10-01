package org.ohdsi.webapi.service.cscompare.config;

import org.ohdsi.webapi.service.cscompare.ConceptSetBatchCompareJobListener;
import org.ohdsi.webapi.service.cscompare.ConceptSetBatchCompareTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConceptSetBatchCompareJobConfig {

	@Autowired
	private ConceptSetBatchCompareJobListener jobListener;

	@Autowired
	private ConceptSetBatchCompareTasklet tasklet;

	@Bean
	public Job conceptSetBatchCompareJob(JobBuilderFactory jobBuilderFactory,
																			 Step conceptSetBatchCompareStep) {
		return jobBuilderFactory.get("conceptSetBatchCompareJob")
			.listener(jobListener)
			.start(conceptSetBatchCompareStep)
			.build();
	}

	@Bean
	public Step conceptSetBatchCompareStep(StepBuilderFactory stepBuilderFactory) {
		return stepBuilderFactory.get("conceptSetBatchCompareStep")
			.tasklet(tasklet)
			.build();
	}
}