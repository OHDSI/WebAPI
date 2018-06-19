package org.ohdsi.webapi.source;

public class SourceDetails extends SourceInfo {

    private String connectionString;

    public SourceDetails(Source s) {
        super(s);
        setConnectionString(s.getSourceConnection());
    }

    public String getConnectionString() {

        return connectionString;
    }

    public void setConnectionString(String connectionString) {

        this.connectionString = connectionString;
    }
}
