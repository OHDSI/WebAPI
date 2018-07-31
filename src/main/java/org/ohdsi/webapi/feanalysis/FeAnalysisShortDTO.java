package org.ohdsi.webapi.feanalysis;

import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.StandardFeatureAnalysisType;

public class FeAnalysisShortDTO {
    
    private Long id;
    private String name;
    private StandardFeatureAnalysisType type;
    private StandardFeatureAnalysisDomain domain;

    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(final String name) {

        this.name = name;
    }

    public StandardFeatureAnalysisType getType() {
        return type;
    }

    public void setType(final StandardFeatureAnalysisType type) {
        this.type = type;
    }

    public StandardFeatureAnalysisDomain getDomain() {
        return domain;
    }

    public void setDomain(final StandardFeatureAnalysisDomain domain) {
        this.domain = domain;
    }
}
