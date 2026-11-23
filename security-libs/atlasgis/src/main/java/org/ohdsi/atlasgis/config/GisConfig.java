package org.ohdsi.atlasgis.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.ohdsi.atlasgis.controller.GisCohortController;
import org.ohdsi.atlasgis.controller.GisPersonController;
import org.ohdsi.atlasgis.controller.GisSourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.PostConstruct;

@Configuration
@DependsOn("jerseyConfig")
public class GisConfig {
    private static final String GIS_PACKAGES = "org.ohdsi.atlasgis";
    @Autowired
    private ResourceConfig jerseyConfig;

    @PostConstruct
    public void init() {
        jerseyConfig.packages(GIS_PACKAGES);
        jerseyConfig.register(GisSourceController.class);
        jerseyConfig.register(GisCohortController.class);
        jerseyConfig.register(GisPersonController.class);
    }
}
