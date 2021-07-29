package org.ohdsi.webapi.cohortdefinition;

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.source.Source;

public class CohortGenerationRequest {

    private CohortExpression expression;
    private Source source;
    private String sessionId;
    private String targetSchema;
    private Integer targetId;

    public CohortGenerationRequest(CohortExpression expression, Source source, String sessionId, Integer targetId, String targetSchema) {

        this.expression = expression;
        this.source = source;
        this.sessionId = sessionId;
        this.targetId = targetId;
        this.targetSchema = targetSchema;
    }

    public CohortExpression getExpression() {

        return expression;
    }

    public Source getSource() {

        return source;
    }

    public String getSessionId() {

        return sessionId;
    }

    public String getTargetSchema() {

        return targetSchema;
    }

    public Integer getTargetId() {

        return targetId;
    }
}
