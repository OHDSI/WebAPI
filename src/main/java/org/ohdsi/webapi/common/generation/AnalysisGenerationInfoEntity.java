package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.shiro.Entities.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_generation_info")
public class AnalysisGenerationInfoEntity {

    @Id
    @Column(name = "job_execution_id")
    private Long id;

    @Embedded
    private AnalysisGenerationInfo info = new AnalysisGenerationInfo();

    public void setId(Long id) {

        this.id = id;
    }

    public String getDesign() {
        return info.getDesign();
    }

    public void setDesign(String serializedDesign) {

        this.info.design = serializedDesign;
        this.info.hashCode = serializedDesign.hashCode();
    }

    public void setCreatedBy(UserEntity createdBy) {

        this.info.createdBy = createdBy;
    }
}
