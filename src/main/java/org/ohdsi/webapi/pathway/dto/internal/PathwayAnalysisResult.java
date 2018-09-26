package org.ohdsi.webapi.pathway.dto.internal;

import java.util.ArrayList;
import java.util.List;

public class PathwayAnalysisResult {

    private List<PathwayCode> codes = new ArrayList<>();
    private List<CohortPathways> cohortPathwaysList = new ArrayList<>();

    public List<PathwayCode> getCodes() {

        return codes;
    }

    public void setCodes(List<PathwayCode> codes) {

        this.codes = codes;
    }

    public List<CohortPathways> getCohortPathwaysList() {

        return cohortPathwaysList;
    }

    public void setCohortPathwaysList(List<CohortPathways> cohortPathwaysList) {

        this.cohortPathwaysList = cohortPathwaysList;
    }
}
