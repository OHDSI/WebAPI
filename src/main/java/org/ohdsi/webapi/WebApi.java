package org.ohdsi.webapi;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;


/**
 * OHDSI WebAPI Spring Boot Application
 *
 * - JAR: java -jar WebAPI.jar (embedded Tomcat)
 * - WAR: Deploy to external servlet container (mvn package -Pwar)
 */
@EnableScheduling
@SpringBootApplication(exclude={HibernateJpaAutoConfiguration.class, ErrorMvcAutoConfiguration.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class WebApi extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(WebApi.class);
    }

    public static void main(final String[] args) throws Exception
    {
        TomcatURLStreamHandlerFactory.disable();
        SpringApplication.run(WebApi.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}
