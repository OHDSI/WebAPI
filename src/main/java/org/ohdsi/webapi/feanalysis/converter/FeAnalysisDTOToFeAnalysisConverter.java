package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.springframework.stereotype.Component;

@Component
public class FeAnalysisDTOToFeAnalysisConverter extends BaseFeAnalysisDTOToFeAnalysisConverter<FeAnalysisEntity> {
    
    @Override
    protected FeAnalysisEntity createResultObject() {
        return new FeAnalysisEntity();
    }
}

