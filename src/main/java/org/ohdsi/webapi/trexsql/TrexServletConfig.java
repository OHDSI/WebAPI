package org.ohdsi.webapi.trexsql;

import com.trex.TrexServlet;
import jakarta.servlet.http.HttpServlet;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for registering the TrexServlet.
 * The servlet handles all /WebAPI/trexsql/* requests via Ring/Reitit routing.
 */
@Configuration
@ConditionalOnProperty(name = "trexsql.enabled", havingValue = "true")
public class TrexServletConfig {

    private static final Logger log = LoggerFactory.getLogger(TrexServletConfig.class);

    @Bean
    public ServletRegistrationBean<HttpServlet> trexServlet(
            TrexsqlInstanceManager instanceManager,
            SourceRepository sourceRepository) {

        log.info("Registering TrexServlet for /WebAPI/trexsql/*");

        TrexServlet servlet = new TrexServlet();
        servlet.initTrex(instanceManager.getInstance(), sourceRepository);

        ServletRegistrationBean<HttpServlet> registration =
            new ServletRegistrationBean<>(servlet, "/WebAPI/trexsql/*");
        registration.setLoadOnStartup(1);
        registration.setName("trexServlet");

        log.info("TrexServlet registered successfully");
        return registration;
    }
}
