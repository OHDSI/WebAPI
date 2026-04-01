package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.springframework.stereotype.Component;

@Component
public class FeAnalysisDTOToFeAnalysisWithStringConverter extends BaseFeAnalysisDTOToFeAnalysisConverter<FeAnalysisDTO, FeAnalysisWithStringEntity> {
    @Override
    public FeAnalysisWithStringEntity convert(final FeAnalysisDTO source) {
        if (source.getType() != StandardFeatureAnalysisType.CUSTOM_FE && source.getType() != StandardFeatureAnalysisType.PRESET) {
            throw new IllegalArgumentException("Only PRESET and CUSTOME_FE analyses can have design of String type");
        }
        final FeAnalysisWithStringEntity baseEntity = super.convert(source);
        baseEntity.setDesign(String.valueOf(source.getDesign()));        
        return baseEntity;
    }

    @Override
    protected FeAnalysisWithStringEntity createResultObject() {
        return new FeAnalysisWithStringEntity();
    }
}
