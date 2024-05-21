package org.ohdsi.webapi.cohortcharacterization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.webapi.cohortdefinition.CohortMetadataExt;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataImplDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;

import java.util.ArrayList;
import java.util.Collection;

public class BaseCcDTO<T extends CohortMetadataImplDTO, F extends FeAnalysisShortDTO> extends CcShortDTO {

  private Collection<T> cohorts = new ArrayList<>();
  private Collection<F> featureAnalyses = new ArrayList<>();
  private Collection<CcParameterDTO> parameters = new ArrayList<>();
  @JsonProperty("stratifiedBy")
  private String stratifiedBy;
  @JsonProperty("strataOnly")
  private Boolean strataOnly;
  private Collection<CcStrataDTO> stratas = new ArrayList<>();
  @JsonProperty("strataConceptSets")
  private Collection<ConceptSet> strataConceptSets = new ArrayList<>();

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

  public Boolean getStrataOnly() {
    return strataOnly;
  }

  public void setStrataOnly(Boolean strataOnly) {
    this.strataOnly = strataOnly;
  }

  public Collection<ConceptSet> getStrataConceptSets() {
    return strataConceptSets;
  }

  public void setStrataConceptSets(Collection<ConceptSet> strataConceptSets) {
    this.strataConceptSets = strataConceptSets;
  }
}
