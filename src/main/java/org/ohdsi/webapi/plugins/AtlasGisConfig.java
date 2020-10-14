package org.ohdsi.webapi.plugins;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "atlasgis.enabled", havingValue = "true")
@ComponentScan("org.ohdsi.atlasgis")
public class AtlasGisConfig {}
