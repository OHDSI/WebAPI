package org.ohdsi.webapi;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;


/**
 * Launch as java application or deploy as WAR (@link {@link WebApplication}
 * will source this file).
 */
@EnableScheduling
@SpringBootApplication(exclude={HibernateJpaAutoConfiguration.class, ErrorMvcAutoConfiguration.class, SolrAutoConfiguration.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class WebApi extends SpringBootServletInitializer {



    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return application.sources(WebApi.class);
    }

    public static void main(final String[] args) throws Exception
    {
        TomcatURLStreamHandlerFactory.disable();
        new SpringApplicationBuilder(WebApi.class).run(args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}
