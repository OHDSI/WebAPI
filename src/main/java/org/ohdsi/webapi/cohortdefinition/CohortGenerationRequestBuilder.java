package org.ohdsi.webapi.cohortdefinition;

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.source.Source;

public class CohortGenerationRequestBuilder {

    private CohortExpression expression;
    private Source source;
    private String sessionId;
    private String targetSchema;
    private String targetTable;
    private String targetIdFieldName;
    private Integer targetId;
    private boolean generateStats;

    public CohortGenerationRequestBuilder(String sessionId, String targetSchema, String targetTable, String targetIdFieldName, boolean generateStats) {

        this.sessionId = sessionId;
        this.targetSchema = targetSchema;
        this.targetTable = targetTable;
        this.targetIdFieldName = targetIdFieldName;
        this.generateStats = generateStats;
    }

    public CohortGenerationRequestBuilder withSource(Source source) {

        this.source = source;
        return this;
    }

    public CohortGenerationRequestBuilder withExpression(CohortExpression expression) {

        this.expression = expression;
        return this;
    }

    public CohortGenerationRequestBuilder withTargetId(Integer targetId) {

        this.targetId = targetId;
        return this;
    }

    public CohortGenerationRequest build() {

        if (this.source == null || this.expression == null || this.targetId == null) {
            throw new RuntimeException("CohortGenerationRequest should contain non-null expression, source and targetId");
        }

        return new CohortGenerationRequest(expression, source, sessionId, targetSchema, targetTable, targetIdFieldName, targetId, generateStats);
    }
}
