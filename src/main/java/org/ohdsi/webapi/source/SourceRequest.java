package org.ohdsi.webapi.source;

import java.util.Collection;
import javax.validation.constraints.NotNull;

public class SourceRequest {
    @NotNull
    private String name;
    @NotNull
    private String dialect;
    @NotNull
    private String key;
    @NotNull
    private String connectionString;
    private Collection<SourceDaimon> daimons;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDialect() {

        return dialect;
    }

    public void setDialect(String dialect) {

        this.dialect = dialect;
    }

    public String getKey() {

        return key;
    }

    public void setKey(String key) {

        this.key = key;
    }

    public String getConnectionString() {

        return connectionString;
    }

    public void setConnectionString(String connectionString) {

        this.connectionString = connectionString;
    }

    public Collection<SourceDaimon> getDaimons() {

        return daimons;
    }

    public void setDaimons(Collection<SourceDaimon> daimons) {

        this.daimons = daimons;
    }
}
