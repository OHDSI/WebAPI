package org.ohdsi.webapi.cohortcharacterization.report;

import org.ohdsi.webapi.cohortcharacterization.dto.CcDistributionStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;

import static org.ohdsi.analysis.cohortcharacterization.design.CcResultType.DISTRIBUTION;
import static org.ohdsi.analysis.cohortcharacterization.design.CcResultType.PREVALENCE;

public class ItemFactory {
    public ExportItem createItem(CcResult ccResult, String cohortName) {
        if (PREVALENCE.equals(ccResult.getResultType())) {
            return new PrevalenceItem((CcPrevalenceStat) ccResult, cohortName);
        } else if (DISTRIBUTION.equals(ccResult.getResultType())) {
            return new DistributionItem((CcDistributionStat) ccResult, cohortName);
        }
        // Type is checked before
        throw new RuntimeException("ExportItem for result type: " + ccResult.getResultType() + " is not defined");
    }
}
