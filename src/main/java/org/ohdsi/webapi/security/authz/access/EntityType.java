package org.ohdsi.webapi.security.authz.access;

import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.source.Source;

public enum EntityType {
    COHORT_DEFINITION(CohortDefinitionEntity.class),
    CONCEPT_SET(ConceptSet.class),
    SOURCE(Source.class),
    COHORT_CHARACTERIZATION(CohortCharacterizationEntity.class),
    FE_ANALYSIS(FeAnalysisEntity.class);
    // PATHWAY_ANALYSIS(PathwayAnalysisEntity.class),
    // FE_ANALYSIS(FeAnalysisEntity.class),
    // INCIDENCE_RATE(IncidenceRateAnalysis.class),
    // COHORT_SAMPLE(CohortSample.class),
    // TAG(Tag.class),
    // TOOL(Tool.class),
    // REUSABLE(Reusable.class);
    private final Class<?> entityClass;

    EntityType(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }
}
