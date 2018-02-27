package org.ohdsi.webapi.cohortcomparison;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cca_execution_ext")
public class CCAExecutionExtension {
    @Id
    @Column(name = "cca_execution_id")
    private Integer id;
    @Column(name = "update_password")
    private String updatePassword;

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public String getUpdatePassword() {

        return updatePassword;
    }

    public void setUpdatePassword(String updatePassword) {

        this.updatePassword = updatePassword;
    }
}
