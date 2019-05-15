package org.ohdsi.webapi.analysis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.SimpleDateFormat;
import org.ohdsi.analysis.Cohort;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;

@JsonIgnoreProperties(ignoreUnknown=true)
public class AnalysisCohortDefinition implements Cohort {
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
  
    /**
     *
     * @param def
     */
    public AnalysisCohortDefinition(CohortDefinition def) {
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
  
    /**
     * Constructor
     */
    public AnalysisCohortDefinition() {
  }
  
    /**
     *
     * @return
     */
    @Override
  public Integer getId() {
    return id;
  }

    /**
     *
     * @param id
     */
    public void setId(Integer id) {
    this.id = id;
  }

    /**
     *
     * @return
     */
    @Override
  public String getName() {
    return name;
  }

    /**
     *
     * @param name
     */
    public void setName(String name) {
    this.name = name;
    }

    /**
     *
     * @return
     */
    @Override
  public String getDescription() {
    return description;
  }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
    this.description = description;
  }
  
    /**
     *
     * @return
     */
    public ExpressionType getExpressionType() {
    return expressionType;
  }
  
    /**
     *
     * @param expressionType
     */
    public void setExpressionType(ExpressionType expressionType) {
    this.expressionType = expressionType;
  }
  
    /**
     *
     * @return
     */
    @Override
  public CohortExpression getExpression() {
      return expression;
  }
  
    /**
     *
     * @param expression
     */
    public void setExpression(CohortExpression expression) {
      this.expression = expression;
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
