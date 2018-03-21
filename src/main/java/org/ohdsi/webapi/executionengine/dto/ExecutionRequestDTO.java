package org.ohdsi.webapi.executionengine.dto;

import org.ohdsi.webapi.executionengine.entity.AnalysisExecutionType;

public class ExecutionRequestDTO {

    public String sourceKey;

    public String template;

    public String exposureTable;

    public String outcomeTable;

    public Integer cdmVersion;

    public String workFolder;

    public Integer cohortId;

    public AnalysisExecutionType analysisType;
}
