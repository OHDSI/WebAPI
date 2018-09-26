package org.ohdsi.webapi;

import com.odysseusinc.logging.LoggingEventMessageFactory;
import com.odysseusinc.logging.LoggingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConfiguration {

    @Bean
    public LoggingEventMessageFactory loggingEventMessageFactory(){
        return new LoggingEventMessageFactory();
    }

    @Bean
    public LoggingService loggingService(LoggingEventMessageFactory factory){
        return new LoggingService(factory);
    }
}
