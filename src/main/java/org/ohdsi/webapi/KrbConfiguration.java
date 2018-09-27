package org.ohdsi.webapi;

import com.odysseusinc.datasourcemanager.krblogin.KerberosService;
import com.odysseusinc.datasourcemanager.krblogin.KerberosServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KrbConfiguration {

    @Value("${kerberos.timeout}")
    private long timeout;
    @Value("${kerberos.kinitPath}")
    private String kinitPath;
    @Value("${kerberos.configPath}")
    private String configPath;

    @Bean
    public KerberosService kerberosService(){
        return new KerberosServiceImpl(timeout, kinitPath, configPath);
    }
}
