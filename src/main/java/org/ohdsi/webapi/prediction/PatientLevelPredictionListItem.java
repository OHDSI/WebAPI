/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.prediction;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;

/**
 *
 * @author asena5
 */
public class PatientLevelPredictionListItem {
    public Integer analysisId;
    public String name;
    public String modelType;
    public String createdBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm")
    public Date createdDate;
    public String modifiedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm")
    public Date modifiedDate;
  }

