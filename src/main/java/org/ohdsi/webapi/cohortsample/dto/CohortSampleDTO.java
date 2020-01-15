package org.ohdsi.webapi.cohortsample.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.ohdsi.webapi.cohortsample.SampleElement;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CohortSampleDTO {
    private int id;
    private int size;


    private Date createdDate;
    private UserEntity createdBy;
    private int cohortDefinitionId;
    private int sourceId;
    private SampleParametersDTO.AgeDTO age;
    private SampleParametersDTO.GenderDTO gender;

    private List<SampleElementDTO> elements;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date setCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public int getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(int cohortDefinitionId) {
        this.cohortDefinitionId = cohortDefinitionId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<SampleElementDTO> getElements() {
        return elements;
    }

    public void setElements(List<SampleElementDTO> elements) {
        this.elements = elements;
    }

    public SampleParametersDTO.AgeDTO getAge() {
        return age;
    }

    public void setAge(SampleParametersDTO.AgeDTO age) {
        this.age = age;
    }

    public SampleParametersDTO.GenderDTO getGender() {
        return gender;
    }

    public void setGender(SampleParametersDTO.GenderDTO gender) {
        this.gender = gender;
    }
}
