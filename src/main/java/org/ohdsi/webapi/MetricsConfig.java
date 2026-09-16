package org.ohdsi.webapi;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the {@link TimedAspect} so that {@code @Timed} annotations
 * on Spring beans are automatically instrumented as Micrometer timers.
 * Metrics are exposed via the Actuator {@code /actuator/metrics} endpoint.
 */
@Configuration
public class MetricsConfig {

  @Bean
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }
}
