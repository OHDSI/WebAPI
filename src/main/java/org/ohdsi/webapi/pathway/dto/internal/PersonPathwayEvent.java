package org.ohdsi.webapi.pathway.dto.internal;

import java.util.Date;

public class PersonPathwayEvent {

    private Integer comboId;
    private Integer subjectId;
    private Date startDate;
    private Date endDate;

    public Integer getComboId() {

        return comboId;
    }

    public void setComboId(Integer comboId) {

        this.comboId = comboId;
    }

    public Integer getSubjectId() {

        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {

        this.subjectId = subjectId;
    }

    public Date getStartDate() {

        return startDate;
    }

    public void setStartDate(Date startDate) {

        this.startDate = startDate;
    }

    public Date getEndDate() {

        return endDate;
    }

    public void setEndDate(Date endDate) {

        this.endDate = endDate;
    }
}
