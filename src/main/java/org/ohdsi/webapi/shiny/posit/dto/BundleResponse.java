package org.ohdsi.webapi.shiny.posit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleResponse {
    private String id;
    @JsonProperty("content_guid")
    private String contentGuid;
    @JsonProperty("created_time")
    private Instant createdTime;
    @JsonProperty("cluster_name")
    private String clusterName;
    @JsonProperty("image_name")
    private String imageName;
    @JsonProperty("r_version")
    private String rVersion;
    @JsonProperty("r_environment_management")
    private Boolean rEnvironmentManagement;
    @JsonProperty("py_version")
    private String pyVersion;
    @JsonProperty("py_environment_management")
    private Boolean pyEnvironmentManagement;
    @JsonProperty("quarto_version")
    private String quartoVersion;
    private Boolean active;
    private Integer size;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContentGuid() {
        return contentGuid;
    }

    public void setContentGuid(String contentGuid) {
        this.contentGuid = contentGuid;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getrVersion() {
        return rVersion;
    }

    public void setrVersion(String rVersion) {
        this.rVersion = rVersion;
    }

    public Boolean getrEnvironmentManagement() {
        return rEnvironmentManagement;
    }

    public void setrEnvironmentManagement(Boolean rEnvironmentManagement) {
        this.rEnvironmentManagement = rEnvironmentManagement;
    }

    public String getPyVersion() {
        return pyVersion;
    }

    public void setPyVersion(String pyVersion) {
        this.pyVersion = pyVersion;
    }

    public Boolean getPyEnvironmentManagement() {
        return pyEnvironmentManagement;
    }

    public void setPyEnvironmentManagement(Boolean pyEnvironmentManagement) {
        this.pyEnvironmentManagement = pyEnvironmentManagement;
    }

    public String getQuartoVersion() {
        return quartoVersion;
    }

    public void setQuartoVersion(String quartoVersion) {
        this.quartoVersion = quartoVersion;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}