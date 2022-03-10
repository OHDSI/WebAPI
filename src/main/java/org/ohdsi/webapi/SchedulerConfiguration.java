package org.ohdsi.webapi;

import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static com.cronutils.model.CronType.QUARTZ;

@Configuration
public class SchedulerConfiguration {

  @Bean
  public TaskScheduler taskScheduler() {

    final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(20);
    taskScheduler.initialize();
    return taskScheduler;
  }

  @Bean
  public CronDefinition cronDefinition() {

    return CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
  }
}
