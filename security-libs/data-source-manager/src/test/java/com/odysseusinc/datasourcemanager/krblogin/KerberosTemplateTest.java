package com.odysseusinc.datasourcemanager.krblogin;

import com.odysseusinc.arachne.commons.utils.TemplateUtils;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class KerberosTemplateTest {
    @Test
    public void actualHeaderRetainsConfigurationLayout() throws Exception {
        String expected = "[libdefaults]\n  default_realm = EXAMPLE.ORG\n\n[realms]\n\n[domain_realm]";
        assertEquals(expected, TemplateUtils.loadTemplate("/templates/krb5ConfHeader.mustache")
                .apply(Collections.singletonMap("defaultRealmName", "EXAMPLE.ORG")));
        Method method = KerberosServiceImpl.class.getDeclaredMethod("buildKrbConfHeader", String.class);
        method.setAccessible(true);
        assertEquals(expected, method.invoke(new KerberosServiceImpl(1, "", ""), "EXAMPLE.ORG"));
    }

    @Test
    public void actualRealmAndServiceRetainConfigurationLayout() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("realmName", "EXAMPLE.ORG");
        values.put("adminServer", "admin.example.org");
        values.put("kdcServer", "kdc.example.org");
        String expected = "\n  EXAMPLE.ORG = {\n    admin_server = admin.example.org\n    kdc = kdc.example.org\n  }\n";
        assertEquals(expected, TemplateUtils.loadTemplate("/templates/krb5ConfRealm.mustache").apply(values));
        DataSourceUnsecuredDTO source = new DataSourceUnsecuredDTO();
        source.setKrbRealm("EXAMPLE.ORG");
        source.setKrbAdminFQDN("admin.example.org");
        source.setKrbFQDN("kdc.example.org");
        Method method = KerberosServiceImpl.class.getDeclaredMethod("buildKrbConfRealm", DataSourceUnsecuredDTO.class);
        method.setAccessible(true);
        assertEquals(expected, method.invoke(new KerberosServiceImpl(1, "", ""), source));
    }

    @Test
    public void actualTemplatesEscapeAndDoNotExpandValues() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("realmName", "日本語$1\\{{adminServer}}");
        values.put("adminServer", "<>\"'`&=");
        assertEquals("\n  日本語$1\\{{adminServer}} = {\n    admin_server = &lt;&gt;&quot;&#x27;&#x60;&amp;&#x3D;\n    kdc = \n  }\n",
                TemplateUtils.loadTemplate("/templates/krb5ConfRealm.mustache").apply(values));
    }
}
