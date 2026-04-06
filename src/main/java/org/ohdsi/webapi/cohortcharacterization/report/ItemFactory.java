package org.ohdsi.webapi.cohortcharacterization.report;

import com.google.common.collect.ImmutableMap;
import org.ohdsi.analysis.cohortcharacterization.design.CcResultType;
import org.ohdsi.webapi.cohortcharacterization.dto.CcDistributionStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.ohdsi.analysis.cohortcharacterization.design.CcResultType.DISTRIBUTION;
import static org.ohdsi.analysis.cohortcharacterization.design.CcResultType.PREVALENCE;

public class ItemFactory {
    private static Map<CcResultType, BiFunction<CcResult, String, ExportItem>> itemMap = ImmutableMap.of(
        PREVALENCE, (ccResult, cohortName) -> new PrevalenceItem((CcPrevalenceStat) ccResult, cohortName),
        DISTRIBUTION,  (ccResult, cohortName) -> new DistributionItem((CcDistributionStat) ccResult, cohortName)
    );

    public ExportItem createItem(CcResult ccResult, String cohortName) {
        return Optional.ofNullable(itemMap.get(ccResult.getResultType()))
                .orElseThrow(() -> new RuntimeException("ExportItem for result type: " + ccResult.getResultType() + " is not defined"))
                .apply(ccResult, cohortName);
    }
}
