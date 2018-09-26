package org.ohdsi.webapi.estimation.specification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.SimpleDateFormat;
import org.ohdsi.analysis.Cohort;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EstimationCohortDefinition implements Cohort {
  String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  private SimpleDateFormat dateFormatter;
  
  @JsonProperty("id")
  private Integer id;
  
  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;
  
  @JsonProperty("expression")
  private CohortExpression expression;
  
  @JsonProperty("expressionType")
  private ExpressionType expressionType;
  
  @JsonProperty("createdBy")
  private String createdBy;
  
  @JsonProperty("createdDate")
  private String createdDate;

  @JsonProperty("modifiedBy")
  private String modifiedBy;
  
  @JsonProperty("modifiedDate")
  private String modifiedDate;
  
  public EstimationCohortDefinition(CohortDefinition def) {
      this.dateFormatter = new SimpleDateFormat(this.dateFormat);
      this.id = def.getId();
      this.name = def.getName();
      this.description = def.getDescription();
      this.expression = def.getDetails().getExpressionObject();
      this.expressionType = def.getExpressionType();
      this.createdBy = def.getCreatedBy() != null ? def.getCreatedBy().getLogin() : null;
      this.createdDate = def.getCreatedDate() != null ? this.dateFormatter.format(def.getCreatedDate()) : null;
      this.modifiedBy = def.getModifiedBy() != null ? def.getModifiedBy().getLogin() : null;
      this.modifiedDate = def.getModifiedDate() != null ? this.dateFormatter.format(def.getModifiedDate()) : null;
  }
  
  public EstimationCohortDefinition() {
  }
  
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public EstimationCohortDefinition setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public EstimationCohortDefinition setDescription(String description) {
    this.description = description;
    return this;
  }
  
  public ExpressionType getExpressionType() {
    return expressionType;
  }
  
  public EstimationCohortDefinition setExpressionType(ExpressionType expressionType) {
    this.expressionType = expressionType;
    return this;
  }
  
  
  public CohortExpression getExpression() {
      return expression;
  }
  
  public EstimationCohortDefinition setExpression(CohortExpression expression) {
      this.expression = expression;
      return this;
  }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the createdDate
     */
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate the createdDate to set
     */
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * @return the modifiedBy
     */
    public String getModifiedBy() {
        return modifiedBy;
    }

    /**
     * @param modifiedBy the modifiedBy to set
     */
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    /**
     * @return the modifiedDate
     */
    public String getModifiedDate() {
        return modifiedDate;
    }

    /**
     * @param modifiedDate the modifiedDate to set
     */
    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }    
}
