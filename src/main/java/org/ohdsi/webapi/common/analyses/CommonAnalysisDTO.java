package org.ohdsi.webapi.common.analyses;

import org.ohdsi.webapi.CommonDTO;
import org.ohdsi.webapi.user.dto.UserDTO;

import java.util.Date;

public class CommonAnalysisDTO  implements CommonDTO {

    private Integer id;
    private String name;
    private String description;    
    private UserDTO createdBy;
    private Date createdDate;
    private UserDTO modifiedBy;
    private Date modifiedDate;

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public UserDTO getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {

        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {

        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {

        this.createdDate = createdDate;
    }

    public UserDTO getModifiedBy() {

        return modifiedBy;
    }

    public void setModifiedBy(UserDTO modifiedBy) {

        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedDate() {

        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {

        this.modifiedDate = modifiedDate;
    }
}
