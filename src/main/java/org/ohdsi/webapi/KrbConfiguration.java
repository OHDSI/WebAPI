package org.ohdsi.webapi;

import com.odysseusinc.krblogin.KerberosService;
import com.odysseusinc.krblogin.KerberosServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KrbConfiguration {

    @Bean
    public KerberosService kerberosService(){
        return new KerberosServiceImpl();
    }
}
