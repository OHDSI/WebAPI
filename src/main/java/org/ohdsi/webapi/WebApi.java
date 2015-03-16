package org.ohdsi.webapi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * Launch as java application or deploy as WAR (@link {@link WebApplication} will source this file).
 */
@SpringBootApplication(exclude={HibernateJpaAutoConfiguration.class,VelocityAutoConfiguration.class})
public class WebApi extends SpringBootServletInitializer {
    
    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return application.sources(WebApi.class);
    }
    
    public static void main(final String[] args) throws Exception {
        new SpringApplicationBuilder(WebApi.class).run(args);
    }
}
