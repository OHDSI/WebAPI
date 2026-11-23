package org.ohdsi.webapi;

import org.springframework.boot.test.context.SpringBootTest;

/** Exercise Shiro's web integration without Spring MVC or optional external identity services. */
@SpringBootTest(classes = WebApi.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "security.provider=AtlasRegularSecurity",
                "security.db.datasource.url=${datasource.url}",
                "security.db.datasource.username=postgres",
                "security.db.datasource.password=postgres",
                "security.auth.windows.enabled=false",
                "security.auth.kerberos.enabled=false",
                "security.auth.ldap.enabled=false",
                "security.auth.ad.enabled=false"
        })
public class JerseyOnlySecurityStartupTest extends JerseyOnlyStartupTest {
}
