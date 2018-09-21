package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;

import java.util.ArrayList;
import java.util.Collection;

public class BaseCcDTO<T extends CohortMetadata, F extends FeAnalysisShortDTO> extends CcShortDTO {

  private Collection<T> cohorts = new ArrayList<>();
  private Collection<F> featureAnalyses = new ArrayList<>();
  private Collection<CcParameterDTO> parameters = new ArrayList<>();

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
}
