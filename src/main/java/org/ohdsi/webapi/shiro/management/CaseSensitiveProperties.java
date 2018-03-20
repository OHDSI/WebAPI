package org.ohdsi.webapi.shiro.management;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.provider")
public class CaseSensitiveProperties {
    private Map<String, Boolean> caseSensitive;

    public Map<String, Boolean> getCaseSensitive() {

        return caseSensitive;
    }

    public void setCaseSensitive(Map<String, Boolean> caseSensitive) {

        this.caseSensitive = caseSensitive;
    }
}
