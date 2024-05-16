package org.ohdsi.webapi.shiny;

import org.ohdsi.webapi.service.ShinyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(ShinyService.class)
@EnableConfigurationProperties(PositConnectProperties.class)
public class ShinyConfiguration {
}
