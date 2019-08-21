package org.ohdsi.webapi.cohortcharacterization.report;

import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;

import java.util.ArrayList;
import java.util.List;

public class CovariateStrataItem {
    private List<CcResult> results = new ArrayList<>();

    public List<CcResult> getResults() {
        return results;
    }
}
