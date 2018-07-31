package org.ohdsi.webapi.cohortcharacterization;

import java.util.Date;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.source.Source;

public class CcGeneration {
    private Long id;
    private CohortCharacterization cohortCharacterization;
    private Source source;
    private String design;
    private Date date;
    private UserEntity createdBy;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CohortCharacterization getCohortCharacterization() {
        return cohortCharacterization;
    }

    public void setCohortCharacterization(final CohortCharacterization cohortCharacterization) {
        this.cohortCharacterization = cohortCharacterization;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(final Source source) {
        this.source = source;
    }

    public String getDesign() {
        return design;
    }

    public void setDesign(final String design) {
        this.design = design;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
