package org.ohdsi.webapi.trexsql;

import org.trex.TrexServlet;
import jakarta.servlet.http.HttpServlet;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "trexsql.enabled", havingValue = "true")
@EnableConfigurationProperties(TrexSQLConfig.class)
public class TrexSQLServletConfig {

    private static final Logger log = LoggerFactory.getLogger(TrexSQLServletConfig.class);

    @Bean
    public ServletRegistrationBean<HttpServlet> trexServlet(
            TrexSQLInstanceManager instanceManager,
            TrexSQLConfig trexConfig,
            SourceRepository sourceRepository) {

        TrexServlet servlet = new TrexServlet();
        Map<String, Object> config = new HashMap<>();
        String cachePath = trexConfig.getCachePath();
        log.info("TrexSQL cache path configured as: {}", cachePath);
        config.put("cache-path", cachePath);

        servlet.initTrex(instanceManager.getInstance(), sourceRepository, config);

        ServletRegistrationBean<HttpServlet> registration =
            new ServletRegistrationBean<>(servlet, "/WebAPI/trexsql/*");
        registration.setLoadOnStartup(1);
        return registration;
    }
}
