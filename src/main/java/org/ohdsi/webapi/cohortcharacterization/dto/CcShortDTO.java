package org.ohdsi.webapi.cohortcharacterization.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import org.ohdsi.webapi.CommonDTO;
import org.ohdsi.webapi.cohortcharacterization.CcConst;
import org.ohdsi.webapi.user.dto.UserDTO;

public class CcShortDTO implements CommonDTO {
    
    private Long id;
    private UserDTO createdBy;
    @JsonFormat(pattern = CcConst.dateFormat)
    private Date createdAt;
    private UserDTO updatedBy;
    @JsonFormat(pattern = CcConst.dateFormat)
    private Date updatedAt;
    private Integer hashCode;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    public UserDTO getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(final UserDTO updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getHashCode() {
        return hashCode;
    }

    public void setHashCode(final Integer hashCode) {
        this.hashCode = hashCode;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
