package org.ohdsi.webapi;

// import org.ohdsi.webapi.arachne.logging.LoggingEventMessageFactory; // Not needed
// import org.ohdsi.webapi.arachne.logging.LoggingService; // Not needed
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConfiguration {

    // Commented out - LoggingService and LoggingEventMessageFactory not available
    // These were part of Arachne logging dependency that was removed
    /*
    @Bean
    public LoggingEventMessageFactory loggingEventMessageFactory(){
        return new LoggingEventMessageFactory();
    }

    @Bean
    public LoggingService loggingService(LoggingEventMessageFactory factory){
        return new LoggingService(factory);
    }
    */
}
