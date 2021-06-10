package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.cohortcharacterization.design.CcResultType;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;

public class FeAnalysisShortDTO extends CommonEntityDTO {

    @JsonProperty("description")
    protected String description;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("type")
    private StandardFeatureAnalysisType type;
    @JsonProperty("domain")
    private StandardFeatureAnalysisDomain domain;
    @JsonProperty("statType")
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
