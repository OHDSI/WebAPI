package com.jnj.honeur.webapi.liferay.model;

import java.io.Serializable;
import java.util.Objects;

public class Organization implements Serializable{
    @Override
    public String toString() {
        return "Organization{" +
                "uuid='" + uuid + '\'' +
                ", organizationId=" + organizationId +
                ", companyId=" + companyId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", createDate='" + createDate + '\'' +
                ", modifiedDate='" + modifiedDate + '\'' +
                ", parentOrganizationId=" + parentOrganizationId +
                ", treePath='" + treePath + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", recursable=" + recursable +
                ", regionId=" + regionId +
                ", countryId=" + countryId +
                ", statusId=" + statusId +
                ", comments='" + comments + '\'' +
                ", logoId=" + logoId +
                ", canRead=" + canRead +
                '}';
    }

    private String uuid;
    private Integer organizationId;
    private Integer companyId;
    private Integer userId;
    private String userName;
    private String createDate;
    private String modifiedDate;
    private Integer parentOrganizationId;
    private String treePath;
    private String name;
    private String type;
    private boolean recursable;
    private Integer regionId;
    private Integer countryId;
    private Integer statusId;
    private String comments;
    private Integer logoId;

    private boolean canRead;

    public Organization() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Integer getParentOrganizationId() {
        return parentOrganizationId;
    }

    public void setParentOrganizationId(Integer parentOrganizationId) {
        this.parentOrganizationId = parentOrganizationId;
    }

    public String getTreePath() {
        return treePath;
    }

    public void setTreePath(String treePath) {
        this.treePath = treePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRecursable() {
        return recursable;
    }

    public void setRecursable(boolean recursable) {
        this.recursable = recursable;
    }

    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getLogoId() {
        return logoId;
    }

    public void setLogoId(Integer logoId) {
        this.logoId = logoId;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization that = (Organization) o;
        return recursable == that.recursable &&
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(organizationId, that.organizationId) &&
                Objects.equals(companyId, that.companyId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(createDate, that.createDate) &&
                Objects.equals(modifiedDate, that.modifiedDate) &&
                Objects.equals(parentOrganizationId, that.parentOrganizationId) &&
                Objects.equals(treePath, that.treePath) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(regionId, that.regionId) &&
                Objects.equals(countryId, that.countryId) &&
                Objects.equals(statusId, that.statusId) &&
                Objects.equals(comments, that.comments) &&
                Objects.equals(logoId, that.logoId);
    }

    @Override
    public int hashCode() {

        return Objects
                .hash(uuid, organizationId, companyId, userId, userName, createDate, modifiedDate, parentOrganizationId,
                        treePath, name, type, recursable, regionId, countryId, statusId, comments, logoId);
    }
}
