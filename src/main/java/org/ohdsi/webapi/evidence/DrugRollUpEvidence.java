package org.ohdsi.webapi.evidence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 *
 * @author rkboyce and ericaVoss
 */
@JsonInclude(Include.NON_NULL)
public class DrugRollUpEvidence {
    @JsonProperty("REPORT_NAME")
    public String reportName;
    
    @JsonProperty("INGREDIENT_ID")
    public Integer ingredientId;

    @JsonProperty("INGREDIENT")
    public String ingredientName;

    @JsonProperty("CLINICAL_DRUG_ID")
    public Integer clinicalDrugId;

    @JsonProperty("CLINICAL_DRUG")
    public String clinicalDrugName;

    @JsonProperty("HOI_ID")
    public Integer hoiId;	

    @JsonProperty("HOI")
    public String hoiName;	
    
    @JsonProperty("CT_COUNT")
    public Integer pubmedMeshCTcount;	

    @JsonProperty("CASE_COUNT")
    public Integer pubmedMeshCaseReportcount;	

    @JsonProperty("OTHER_COUNT")
    public Integer pubmedMeshOthercount;	

    @JsonProperty("SPLICER_COUNT")
    public Integer splicerCount;	

    @JsonProperty("EU_SPC_COUNT")
    public Integer euSPCcount;	

    @JsonProperty("SEMMEDDB_CT_COUNT")
    public Integer semmedCTcount;	

    @JsonProperty("SEMMEDDB_CASE_COUNT")
    public Integer semmedCaseReportcount;	

    @JsonProperty("SEMMEDDB_NEG_CT_COUNT")
    public Integer semmedNegCTcount;	

    @JsonProperty("SEMMEDDB_NEG_CASE_COUNT")
    public Integer semmedNegCaseReportcount;	

    @JsonProperty("AERS_REPORT_COUNT")
    public Integer aersReportCount;	

    @JsonProperty("PRR")
    public BigDecimal prr;

}
