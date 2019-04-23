package org.ohdsi.webapi.estimation;

import java.util.Date;
import org.ohdsi.analysis.estimation.design.EstimationTypeEnum;

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
