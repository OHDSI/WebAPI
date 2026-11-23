package org.ohdsi.webapi;

import org.junit.Test;
import org.springframework.util.ClassUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MinimalWebDependenciesTest {
    @Test
    public void jerseyRemainsAvailableWithoutMvcOrSpringCrypto() {
        ClassLoader loader = getClass().getClassLoader();
        assertTrue(ClassUtils.isPresent("org.glassfish.jersey.servlet.ServletContainer", loader));
        assertFalse(ClassUtils.isPresent("org.springframework.web.servlet.DispatcherServlet", loader));
        assertFalse(ClassUtils.isPresent("org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder", loader));
        assertFalse(ClassUtils.isPresent("com.github.jknack.handlebars.Handlebars", loader));
        assertFalse(ClassUtils.isPresent("org.springframework.ldap.core.LdapTemplate", loader));
    }
}
