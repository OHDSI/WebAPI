package org.ohdsi.webapi.pathway.dto.internal;

import java.util.HashSet;
import java.util.Set;

public class PathwayAnalysisResult {

    private Set<PathwayCode> codes = new HashSet<>();
    private Set<CohortPathways> cohortPathwaysList = new HashSet<>();

    public Set<PathwayCode> getCodes() {

        return codes;
    }

    public void setCodes(Set<PathwayCode> codes) {

        this.codes = codes;
    }

    public Set<CohortPathways> getCohortPathwaysList() {

        return cohortPathwaysList;
    }

    public void setCohortPathwaysList(Set<CohortPathways> cohortPathwaysList) {

        this.cohortPathwaysList = cohortPathwaysList;
    }
}
