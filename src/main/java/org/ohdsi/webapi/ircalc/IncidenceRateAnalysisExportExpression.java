package org.ohdsi.webapi.ircalc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;

import java.util.ArrayList;
import java.util.List;

public class IncidenceRateAnalysisExportExpression extends IncidenceRateAnalysisExpression {

  @JsonProperty("targetCohorts")
  public List<CohortDTO> targetCohorts = new ArrayList<>();

  @JsonProperty("outcomeCohorts")
  public List<CohortDTO> outcomeCohorts = new ArrayList<>();
}
