package org.ohdsi.webapi.feanalysis;

import org.springframework.stereotype.Component;

@Component
public class FeAnalysisDTOToFeAnalysisConverter extends BaseFeAnalysisDTOToFeAnalysisConverter<FeAnalysisEntity> {
    
    @Override
    protected FeAnalysisEntity createResultObject() {
        return new FeAnalysisEntity();
    }
}

