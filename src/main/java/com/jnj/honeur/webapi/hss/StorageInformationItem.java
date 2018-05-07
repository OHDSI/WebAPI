package com.jnj.honeur.webapi.hss;

import java.util.Date;
import java.util.Objects;

public class StorageInformationItem {

    private String uuid;
    private String originalFilename;
    private Date lastModified;
    private String key;

    public StorageInformationItem() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageInformationItem that = (StorageInformationItem) o;
        return Objects.equals(uuid, that.uuid) &&
                Objects.equals(originalFilename, that.originalFilename) &&
                Objects.equals(lastModified, that.lastModified) &&
                Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {

        return Objects.hash(uuid, originalFilename, lastModified, key);
    }
}
