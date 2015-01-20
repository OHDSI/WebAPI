package org.ohdsi.webapi;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class JerseyConfig extends ResourceConfig implements InitializingBean {
    
    @Value("${jersey.resources.root.package}")
    private String rootPackage;
    
    public JerseyConfig() {
    }
    
    /* (non-Jsdoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        packages(this.rootPackage);
    }
    
}
