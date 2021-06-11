package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;
import org.ohdsi.circe.vocabulary.Concept;

@JsonTypeName("TreatmentLine")
public class TreatmentLine extends Criteria {
    @JsonProperty("CodesetId")
    public Integer codesetId;
    @JsonProperty("First")
    public Boolean first;
    @JsonProperty("TreatmentLineStartDate")
    public DateRange treatmentLineStartDate;
    @JsonProperty("TreatmentLineEndDate")
    public DateRange treatmentLineEndDate;
    @JsonProperty("TreatmentLineDrugEraStartDate")
    public DateRange treatmentLineDrugEraStartDate;
    @JsonProperty("TreatmentLineDrugEraEndDate")
    public DateRange treatmentLineDrugEraEndDate;
    @JsonProperty("TreatmentLineNumber")
    public NumericRange treatmentLineNumber;
    @JsonProperty("TotalCycleNumber")
    public NumericRange totalCycleNumber;
    @JsonProperty("DrugExposureCount")
    public NumericRange drugExposureCount;
    @JsonProperty("TreatmentLineType")
    public Concept[] treatmentLineType;
    @JsonProperty("Age")
    public NumericRange age;
    @JsonProperty("Gender")
    public Concept[] gender;

    @Override
    public String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options) {
        return dispatcher.getCriteriaSql(this, options);
    }
}