package org.ohdsi.webapi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfiguration {

    @Primary
    @Bean
    public ObjectMapper objectMapper() {

        return new ObjectMapper();
    }
}
