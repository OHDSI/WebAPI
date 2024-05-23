package org.ohdsi.webapi;

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;

@Configuration
public class ConverterConfiguration {

	@Primary
    @Bean
    GenericConversionService conversionService(){
        return new DefaultConversionService();
    }
    
    @Bean
    ConverterUtils converterUtils(final GenericConversionService conversionService) {
        return new ConverterUtils(conversionService);
    }
}
