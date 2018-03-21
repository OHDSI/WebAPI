package org.ohdsi.webapi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "impala.enabled", havingValue = "true")
public class ImpalaConfig {

    public ImpalaConfig() throws ClassNotFoundException {

        Class.forName("com.cloudera.impala.jdbc41.Driver");
    }
}
