package org.ohdsi.webapi.cohortcharacterization.report;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ohdsi.analysis.cohortcharacterization.design.CcResultType;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

import java.util.*;

public class AnalysisItem {
    // Key is covariate id and strata id
    private Map<Pair<Long, Long>, List<CcResult>> map = new HashMap<>();
    private CcResultType type;
    private String name;
    private String faType;

    public List<CcResult> getOrCreateCovariateItem(Long covariateId, Long strataId) {
        Pair<Long, Long> key = new ImmutablePair<>(covariateId, strataId);
        map.putIfAbsent(key, new ArrayList<>());
        return map.get(key);
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
        Set<Cohort> cohorts = new HashSet<>();
        ItemFactory factory = new ItemFactory();
        for (List<CcResult> results : map.values()) {
            for (CcResult ccResult : results) {
                CohortDefinition cohortDef = definitionMap.get(ccResult.getCohortId());
                ExportItem item = factory.createItem(ccResult, cohortDef.getName());
                String domainId = feAnalysisMap.get(ccResult.getAnalysisName());
                item.setDomainId(domainId);
                domainIds.add(domainId);
                cohorts.add(new Cohort(cohortDef.getId(), cohortDef.getName()));
                values.add(item);
            }
        }
        return new AnalysisResultItem(domainIds, cohorts, values);
    }

    public AnalysisResultItem getComparativeItems(CohortDefinition firstCohortDef, CohortDefinition secondCohortDef,
                                                  Map<String, String> feAnalysisMap) {
        Set<ExportItem> values = new HashSet<>();
        Set<String> domainIds = new HashSet<>();
        Set<Cohort> cohorts = new HashSet<>();
        cohorts.add(new Cohort(firstCohortDef.getId(), firstCohortDef.getName()));
        cohorts.add(new Cohort(secondCohortDef.getId(), secondCohortDef.getName()));
        ItemFactory factory = new ItemFactory();
        for (List<CcResult> results : map.values()) {
            // create default items, because we can have result for only one cohort
            ExportItem first = null;
            ExportItem second = null;
            for (CcResult ccResult : results) {
                if (Objects.equals(ccResult.getCohortId(), firstCohortDef.getId())) {
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
            String domainId = feAnalysisMap.get(comparativeItem.getAnalysisName());
            comparativeItem.setDomainId(domainId);
            domainIds.add(domainId);
            values.add(comparativeItem);
        }
        return new AnalysisResultItem(domainIds, cohorts, values);
    }
}
