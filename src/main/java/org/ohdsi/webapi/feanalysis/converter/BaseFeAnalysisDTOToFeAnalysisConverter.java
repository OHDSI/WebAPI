package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;

public abstract class BaseFeAnalysisDTOToFeAnalysisConverter<T extends FeAnalysisEntity> extends BaseFeAnalysisShortDTOToFeAnalysisConverter<FeAnalysisDTO, T> {

    @Override
    public T convert(final FeAnalysisDTO source) {
        final T result = super.convert(source);
        
        result.setDesign(source.getDesign());
        return result;
    }
}
