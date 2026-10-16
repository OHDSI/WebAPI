package org.ohdsi.webapi.plugins;

public interface WebApiPlugin {
    String getId();
    String getName();
    String getVersion();
    boolean isActive();
}
