package org.ohdsi.webapi;

import org.ohdsi.webapi.arachne.commons.utils.ConverterUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;

@Configuration
public class ConverterConfiguration {

    @Bean
    @Primary
    public GenericConversionService conversionService(){
        return new DefaultConversionService();
    }
    
    @Bean
    public ConverterUtils converterUtils(@Qualifier("conversionService") final GenericConversionService conversionService) {
        return new ConverterUtils(conversionService);
    }
}
