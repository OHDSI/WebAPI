package org.ohdsi.webapi.cohortcharacterization.report;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ohdsi.analysis.cohortcharacterization.design.CcResultType;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnalysisItem {
    // Key is covariate id and strata id
    private Map<Pair<Long, Long>, CovariateStrataItem> map = new HashMap();
    private CcResultType type;
    private String name;
    private String faType;

    public CovariateStrataItem getOrCreateCovariateItem(Long covariateId, Long strataId) {
        Pair<Long, Long> key = new ImmutablePair<>(covariateId, strataId);
        CovariateStrataItem covariateStrataItem = map.get(key);
        if (covariateStrataItem == null) {
            covariateStrataItem = new CovariateStrataItem();
            map.put(key, covariateStrataItem);
        }
        return covariateStrataItem;
    }

    public void setType(CcResultType resultType) {
        this.type = resultType;
    }

    public CcResultType getType() {
        return this.type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFaType(String faType) {
        this.faType = faType;
    }

    public String getFaType() {
        return faType;
    }

    public AnalysisResultItem getSimpleItems(Map<Integer, CohortDefinition> definitionMap,
                                             Map<String, String> feAnalysisMap) {
        Set<ExportItem> values = new HashSet<>();
        Set<String> domainIds = new HashSet<>();
        Set<Pair<Integer, String>> cohorts = new HashSet<>();
        ItemFactory factory = new ItemFactory();
        for (CovariateStrataItem covariateStrataItem : map.values()) {
            for (CcResult ccResult : covariateStrataItem.getResults()) {
                CohortDefinition cohortDef = definitionMap.get(ccResult.getCohortId());
                ExportItem item = factory.createItem(ccResult, cohortDef.getName());
                item.domainId = feAnalysisMap.get(ccResult.getAnalysisName());
                domainIds.add(item.domainId);
                cohorts.add(new ImmutablePair<>(cohortDef.getId(), cohortDef.getName()));
                values.add(item);
            }
        }
        return new AnalysisResultItem(domainIds, cohorts, values);
    }

    public AnalysisResultItem getComparativeItems(CohortDefinition firstCohortDef, CohortDefinition secondCohortDef,
                                                  Map<String, String> feAnalysisMap) {
        Set<ExportItem> values = new HashSet<>();
        Set<String> domainIds = new HashSet<>();
        Set<Pair<Integer, String>> cohorts = new HashSet<>();
        cohorts.add(new ImmutablePair<>(firstCohortDef.getId(), firstCohortDef.getName()));
        cohorts.add(new ImmutablePair<>(secondCohortDef.getId(), secondCohortDef.getName()));
        ItemFactory factory = new ItemFactory();
        for (CovariateStrataItem covariateStrataItem : map.values()) {
            // create default items, because we can have result for only one cohort
            ExportItem first = null;
            ExportItem second = null;
            for (CcResult ccResult : covariateStrataItem.getResults()) {
                if (ccResult.getCohortId() == firstCohortDef.getId()) {
                    first = factory.createItem(ccResult, firstCohortDef.getName());
                } else {
                    second = factory.createItem(ccResult, secondCohortDef.getName());
                }
            }
            ExportItem comparativeItem;
            if(first instanceof DistributionItem || second instanceof DistributionItem) {
                comparativeItem = new ComparativeDistributionItem((DistributionItem) first, (DistributionItem) second,
                        firstCohortDef, secondCohortDef);
            } else {
                comparativeItem = new ComparativeItem((PrevalenceItem) first, (PrevalenceItem) second,
                        firstCohortDef, secondCohortDef);
            }
            comparativeItem.domainId = feAnalysisMap.get(comparativeItem.analysisName);
            domainIds.add(comparativeItem.domainId);
            values.add(comparativeItem);
        }
        return new AnalysisResultItem(domainIds, cohorts, values);
    }
}
