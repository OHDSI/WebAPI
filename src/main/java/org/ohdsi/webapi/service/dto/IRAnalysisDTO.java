package org.ohdsi.webapi.service.dto;

public class IRAnalysisDTO extends IRAnalysisShortDTO {
    
    private String expression;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
