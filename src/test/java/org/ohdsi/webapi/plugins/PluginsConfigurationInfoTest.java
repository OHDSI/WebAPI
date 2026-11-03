package org.ohdsi.webapi.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

public class PluginsConfigurationInfoTest {

    private static WebApiPlugin plugin(String id, AtomicBoolean active) {

        return new WebApiPlugin() {
            public String getId() { return id; }
            public String getName() { return id; }
            public String getVersion() { return "1.0"; }
            public boolean isActive() { return active.get(); }
        };
    }

    @Test
    public void reportsPluginActivationAtReadTimeNotConstructionTime() {

        AtomicBoolean active = new AtomicBoolean(false);
        PluginsConfigurationInfo info =
                new PluginsConfigurationInfo(Collections.singletonList(plugin("trexsql", active)), false);

        assertEquals(false, info.getProperties().get("trexsqlEnabled"));

        active.set(true);
        assertEquals(true, info.getProperties().get("trexsqlEnabled"));

        active.set(false);
        assertEquals(false, info.getProperties().get("trexsqlEnabled"));
    }

    @Test
    public void omitsPluginKeysWhenNoPluginsAreRegistered() {

        PluginsConfigurationInfo info = new PluginsConfigurationInfo(null, true);

        assertEquals(1, info.getProperties().size());
        assertEquals(true, info.getProperties().get("atlasgisEnabled"));
        assertTrue(!info.getProperties().containsKey("trexsqlEnabled"));
    }
}
