package org.ohdsi.webapi.configuration;

import org.ohdsi.analysis.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.SpringHandlerInstantiator;

import javax.annotation.PostConstruct;

@Configuration
public class JacksonConfiguration {

    @Autowired
	private ApplicationContext applicationContext;

    @PostConstruct
    public void configureUtilsMapper() {

        Utils.setObjectMapperHandlerInstantiator(new SpringHandlerInstantiator(this.applicationContext.getAutowireCapableBeanFactory()));
    }
}
