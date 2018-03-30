package org.ohdsi.webapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;

@Configuration
public class ConverterConfiguration {

    @Bean
    public GenericConversionService conversionService(){
        return new DefaultConversionService();
    }
}
