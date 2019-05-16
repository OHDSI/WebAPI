package org.ohdsi.webapi.estimation;

import org.ohdsi.analysis.estimation.design.EstimationTypeEnum;

import java.util.Date;

public class EstimationListItem {
    public Integer estimationId;
    public String name;
    public EstimationTypeEnum type;
    public String description;
    public String createdBy;
    public Date createdDate;
    public String modifiedBy;
    public Date modifiedDate;
}
