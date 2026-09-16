package org.ohdsi.webapi.ircalc.dto;

public class IRAnalysisDTO extends IRAnalysisShortDTO {
    
    private String expression;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
