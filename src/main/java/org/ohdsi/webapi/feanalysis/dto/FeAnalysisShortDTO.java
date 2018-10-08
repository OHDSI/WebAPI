package org.ohdsi.webapi.feanalysis.dto;

import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortcharacterization.CcResultType;

public class FeAnalysisShortDTO {

    protected String description;
    private Integer id;
    private String name;
    private StandardFeatureAnalysisType type;
    private StandardFeatureAnalysisDomain domain;
    private CcResultType statType;

    public Integer getId() {

        return id;
    }

    public void setId(final Integer id) {

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

    public String getDescription() {

        return description;
    }

    public void setDescription(final String description) {

        this.description = description;
    }

    public CcResultType getStatType() {

        return statType;
    }

    public void setStatType(CcResultType statType) {

        this.statType = statType;
    }
}
