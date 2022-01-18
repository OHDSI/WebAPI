package org.ohdsi.webapi.security.model;

import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortsample.CohortSample;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.reusable.domain.Reusable;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.tag.domain.Tag;

public enum EntityType {
    COHORT_DEFINITION(CohortDefinition.class),
    CONCEPT_SET(ConceptSet.class),
    COHORT_CHARACTERIZATION(CohortCharacterizationEntity.class),
    PATHWAY_ANALYSIS(PathwayAnalysisEntity.class),
    FE_ANALYSIS(FeAnalysisEntity.class),
    INCIDENCE_RATE(IncidenceRateAnalysis.class),
    SOURCE(Source.class),
    ESTIMATION(Estimation.class),
    PREDICTION(PredictionAnalysis.class),
    COHORT_SAMPLE(CohortSample.class),
    TAG(Tag.class),
    REUSABLE(Reusable.class);

    private final Class<? extends CommonEntity> entityClass;

    EntityType(Class<? extends CommonEntity> entityClass) {

        this.entityClass = entityClass;
    }

    public Class<? extends CommonEntity> getEntityClass() {

        return entityClass;
    }
}
