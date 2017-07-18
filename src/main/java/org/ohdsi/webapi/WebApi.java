package org.ohdsi.webapi;

import javax.annotation.PostConstruct;
import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;


/**
 * Launch as java application or deploy as WAR (@link {@link WebApplication}
 * will source this file).
 */
@SpringBootApplication(exclude={HibernateJpaAutoConfiguration.class})
public class WebApi extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return application.sources(WebApi.class);
    }

    public static void main(final String[] args) throws Exception {
        new SpringApplicationBuilder(WebApi.class).run(args);
    }

    @Autowired
    private CDMResultsService resultsService;

    @Autowired
    private SourceService sourceService;

    @PostConstruct
    public void warmCaches() {
        sourceService.getSources().stream().forEach((s) -> {
            for (SourceDaimon sd : s.daimons) {
                if (sd.getDaimonType() == SourceDaimon.DaimonType.Results) {
                    resultsService.warmCache(s.sourceKey);
                }
            }
        });
    }
}
