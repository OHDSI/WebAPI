package org.ohdsi.webapi.cohortcharacterization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;

import java.util.ArrayList;
import java.util.Collection;

public class BaseCcDTO<T extends CohortMetadata, F extends FeAnalysisShortDTO> extends CcShortDTO {

  private Collection<T> cohorts = new ArrayList<>();
  private Collection<F> featureAnalyses = new ArrayList<>();
  private Collection<CcParameterDTO> parameters = new ArrayList<>();
  @JsonProperty("stratifiedBy")
  private String stratifiedBy;
  private Collection<CcStrataDTO> stratas = new ArrayList<>();
  @JsonProperty("conceptSets")
  private Collection<ConceptSet> conceptSets = new ArrayList<>();

  public Collection<T> getCohorts() {
    return cohorts;
  }

  public void setCohorts(Collection<T> cohorts) {
    this.cohorts = cohorts;
  }

  public Collection<CcParameterDTO> getParameters() {
      return parameters;
  }

  public void setParameters(final Collection<CcParameterDTO> parameters) {
      this.parameters = parameters;
  }

  public Collection<F> getFeatureAnalyses() {

      return featureAnalyses;
  }

  public void setFeatureAnalyses(final Collection<F> featureAnalyses) {

      this.featureAnalyses = featureAnalyses;
  }

  public Collection<CcStrataDTO> getStratas() {
    return stratas;
  }

  public void setStratas(Collection<CcStrataDTO> stratas) {
    this.stratas = stratas;
  }

  public String getStratifiedBy() {
    return stratifiedBy;
  }

  public void setStratifiedBy(String stratifiedBy) {
    this.stratifiedBy = stratifiedBy;
  }

  public Collection<ConceptSet> getConceptSets() {
    return conceptSets;
  }

  public void setConceptSets(Collection<ConceptSet> conceptSets) {
    this.conceptSets = conceptSets;
  }
}
