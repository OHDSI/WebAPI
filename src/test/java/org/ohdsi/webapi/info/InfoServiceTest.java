package org.ohdsi.webapi.info;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.ohdsi.webapi.plugins.PluginsConfigurationInfo;
import org.ohdsi.webapi.plugins.WebApiPlugin;
import org.springframework.boot.info.BuildProperties;

public class InfoServiceTest {

    @Test
    public void infoReflectsPluginStateAtRequestTime() {

        AtomicBoolean active = new AtomicBoolean(false);
        WebApiPlugin plugin = mock(WebApiPlugin.class);
        when(plugin.getId()).thenReturn("trexsql");
        when(plugin.isActive()).thenAnswer(invocation -> active.get());

        BuildProperties buildProperties = mock(BuildProperties.class);
        when(buildProperties.getVersion()).thenReturn("3.0.0-SNAPSHOT");

        InfoService service = new InfoService(
                buildProperties,
                mock(BuildInfo.class),
                Collections.singletonList(new PluginsConfigurationInfo(Collections.singletonList(plugin), false)));

        assertEquals("3.0.0", service.getInfo().getVersion());
        assertEquals(false, service.getInfo().getConfiguration().get("plugins").get("trexsqlEnabled"));

        active.set(true);
        assertEquals(true, service.getInfo().getConfiguration().get("plugins").get("trexsqlEnabled"));
    }
}
