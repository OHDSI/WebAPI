package org.ohdsi.webapi;

import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Component
public class JerseyConfig extends ResourceConfig implements InitializingBean {
    
    @Value("${jersey.resources.root.package}")
    private String rootPackage;

    @Value("${jersey.resources.additional.packages}")
    private String[] additionalPackages;
    
    public JerseyConfig() {
       EncodingFilter.enableFor(this, GZipEncoder.class);
    }
    
    /* (non-Jsdoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        String[] packages = new String[additionalPackages.length+1];

        packages[0] = rootPackage;
        System.arraycopy(additionalPackages, 0, packages, 1, additionalPackages.length);

        packages(packages);
    }
    
}
