package org.ohdsi.webapi.cohortdefinition;

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.source.Source;

public class CohortGenerationRequestBuilder {

    private CohortExpression expression;
    private Source source;
    private String sessionId;
    private String targetSchema;
    private Integer targetId;
    private Boolean retainCohortCovariates;

    public CohortGenerationRequestBuilder(String sessionId, String targetSchema) {

        this.sessionId = sessionId;
        this.targetSchema = targetSchema;
    }

    public CohortGenerationRequestBuilder(String sessionId, String targetSchema, Boolean retainCohortCovariates) {

        this.sessionId = sessionId;
        this.targetSchema = targetSchema;
        this.retainCohortCovariates = retainCohortCovariates;
    }

    public Boolean getRetainCohortCovariates() {
        return retainCohortCovariates;
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

        return new CohortGenerationRequest(expression, source, sessionId, targetId, targetSchema);
    }

    public CohortGenerationRequest buildWithRetainCohortCovariates() {

        if (this.source == null || this.expression == null || this.targetId == null || this.retainCohortCovariates == null) {
            throw new RuntimeException("CohortGenerationRequest should contain non-null expression, source and targetId");
        }

        return new CohortGenerationRequest(expression, source, sessionId, targetId, targetSchema, retainCohortCovariates);
    }
}
