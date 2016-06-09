package org.ohdsi.webapi.characterization;

public class Datasource {
    private String name;
    private String cdmVersion;
    private String uri;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCDMVersion(String cdmVersion) {
        this.cdmVersion = cdmVersion;
    }

    public String getCMDVersion() {
        return cdmVersion;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

}
