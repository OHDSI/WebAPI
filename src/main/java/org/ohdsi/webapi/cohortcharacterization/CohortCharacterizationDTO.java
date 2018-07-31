package org.ohdsi.webapi.cohortcharacterization;

import java.util.Collection;
import java.util.List;
import org.ohdsi.webapi.feanalysis.FeAnalysisDTO;

public class CohortCharacterizationDTO extends CcShortDTO {
    
    private Collection<CohortDTO> cohorts;
    private Collection<FeAnalysisDTO> featureAnalyses;
    private Collection<CcParameterDTO> parameters;

    public Collection<CohortDTO> getCohorts() {
        return cohorts;
    }

    public void setCohorts(final Collection<CohortDTO> cohorts) {
        this.cohorts = cohorts;
    }

    public Collection<CcParameterDTO> getParameters() {
        return parameters;
    }

    public void setParameters(final Collection<CcParameterDTO> parameters) {
        this.parameters = parameters;
    }

    public Collection<FeAnalysisDTO> getFeatureAnalyses() {

        return featureAnalyses;
    }

    public void setFeatureAnalyses(final Collection<FeAnalysisDTO> featureAnalyses) {

        
        this.featureAnalyses = featureAnalyses;
    }
}
