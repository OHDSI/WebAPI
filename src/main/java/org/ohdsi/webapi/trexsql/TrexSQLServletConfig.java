package org.ohdsi.webapi.trexsql;

import org.trex.TrexServlet;
import jakarta.servlet.http.HttpServlet;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "trexsql.enabled", havingValue = "true")
public class TrexSQLServletConfig {

    @Bean
        public ServletRegistrationBean<HttpServlet> trexServlet(
            TrexSQLInstanceManager instanceManager,
            SourceRepository sourceRepository) {

        TrexServlet servlet = new TrexServlet();
        servlet.initTrex(instanceManager.getInstance(), sourceRepository);

        ServletRegistrationBean<HttpServlet> registration =
            new ServletRegistrationBean<>(servlet, "/WebAPI/trexsql/*");
        registration.setLoadOnStartup(1);
        return registration;
    }
}
