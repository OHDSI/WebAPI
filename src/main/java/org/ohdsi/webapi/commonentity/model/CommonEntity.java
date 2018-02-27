package org.ohdsi.webapi.commonentity.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "common_entity")
public class CommonEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "guid")
    @NotNull
    private String guid;
    @Column(name = "target_entity")
    @NotNull
    private String targetEntity;
    @Column(name = "local_id")
    @NotNull
    private Integer localId;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getGuid() {

        return guid;
    }

    public void setGuid(String guid) {

        this.guid = guid;
    }

    public String getTargetEntity() {

        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {

        this.targetEntity = targetEntity;
    }

    public Integer getLocalId() {

        return localId;
    }

    public void setLocalId(Integer localId) {

        this.localId = localId;
    }
}
