package org.ohdsi.webapi.security.model;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortsample.CohortSample;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.reusable.domain.Reusable;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tool.Tool;

public enum EntityType {
    COHORT_DEFINITION(CohortDefinition.class),
    CONCEPT_SET(ConceptSet.class),
    SOURCE(Source.class),
    COHORT_SAMPLE(CohortSample.class),
    TAG(Tag.class),
    TOOL(Tool.class),
    REUSABLE(Reusable.class);
    private final Class<? extends CommonEntity> entityClass;

    EntityType(Class<? extends CommonEntity> entityClass) {

        this.entityClass = entityClass;
    }

    public Class<? extends CommonEntity> getEntityClass() {

        return entityClass;
    }
}
