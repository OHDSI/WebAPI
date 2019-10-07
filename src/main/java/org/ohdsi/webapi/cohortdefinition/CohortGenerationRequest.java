package org.ohdsi.webapi.cohortdefinition;

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.source.Source;

public class CohortGenerationRequest {

    private CohortExpression expression;
    private Source source;
    private String sessionId;
    private String targetSchema;
    private String targetTable;
    private String targetIdFieldName;
    private Integer targetId;
    private boolean generateStats;

    public CohortGenerationRequest(CohortExpression expression, Source source, String sessionId, String targetSchema, String targetTable, String targetIdFieldName, Integer targetId, boolean generateStats) {

        this.expression = expression;
        this.source = source;
        this.sessionId = sessionId;
        this.targetSchema = targetSchema;
        this.targetTable = targetTable;
        this.targetIdFieldName = targetIdFieldName;
        this.targetId = targetId;
        this.generateStats = generateStats;
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

    public String getTargetTable() {

        return targetTable;
    }

    public String getTargetIdFieldName() {

        return targetIdFieldName;
    }

    public Integer getTargetId() {

        return targetId;
    }

    public boolean isGenerateStats() {

        return generateStats;
    }
}
