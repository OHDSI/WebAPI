package org.ohdsi.webapi.shiny.posit;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TagMapper {

    @Value("${shiny.tag.name.analysis.cohort:Atlas Cohort}")
    private String cohortAnalysisTagName;
    @Value("${shiny.tag.name.analysis.cohort-characterizations:Atlas Cohort Characterization}")
    private String cohortCharacterizationsAnalysisTagName;
    @Value("${shiny.tag.name.analysis.cohort-pathways:Atlas Cohort Pathways}")
    private String cohortPathwaysAnalysisTagName;
    @Value("${shiny.tag.name.analysis.incidence-rates:Atlas Incidence Rate Analysis}")
    private String incidenceRatesAnalysisTagName;

    public String getPositTagNameForAnalysisType(CommonAnalysisType analysisType) {
        if (analysisType == CommonAnalysisType.COHORT) {
            return cohortAnalysisTagName;
        } else if (analysisType == CommonAnalysisType.COHORT_CHARACTERIZATION) {
            return cohortCharacterizationsAnalysisTagName;
        } else if (analysisType == CommonAnalysisType.COHORT_PATHWAY) {
            return cohortPathwaysAnalysisTagName;
        } else if (analysisType == CommonAnalysisType.INCIDENCE) {
            return incidenceRatesAnalysisTagName;
        } else {
            throw new UnsupportedOperationException("Unsupported analysis mapping requested: " + analysisType.getTitle());
        }
    }
}
