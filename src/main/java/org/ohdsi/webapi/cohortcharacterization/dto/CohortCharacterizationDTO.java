package org.ohdsi.webapi.cohortcharacterization.dto;

import java.util.ArrayList;
import java.util.Collection;

import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;

public class CohortCharacterizationDTO extends CcShortDTO {
    
    private Collection<CohortDTO> cohorts = new ArrayList<>();
    private Collection<FeAnalysisDTO> featureAnalyses = new ArrayList<>();
    private Collection<CcParameterDTO> parameters = new ArrayList<>();

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
