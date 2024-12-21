package org.ohdsi.webapi.configuration;

import jakarta.annotation.PostConstruct;
import org.ohdsi.analysis.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.SpringHandlerInstantiator;

@Configuration
public class JacksonConfiguration {

    @Autowired
	private ApplicationContext applicationContext;

    @PostConstruct
    public void configureUtilsMapper() {

        Utils.setObjectMapperHandlerInstantiator(new SpringHandlerInstantiator(this.applicationContext.getAutowireCapableBeanFactory()));
    }
}
