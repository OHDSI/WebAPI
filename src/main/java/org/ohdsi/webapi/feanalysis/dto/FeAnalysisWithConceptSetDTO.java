package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

import java.util.List;

public class FeAnalysisWithConceptSetDTO extends FeAnalysisDTO {

  @JsonProperty("conceptSets")
  private List<ConceptSet> conceptSets;

  public List<ConceptSet> getConceptSets() {
    return conceptSets;
  }

  public void setConceptSets(List<ConceptSet> conceptSets) {
    this.conceptSets = conceptSets;
  }

}
