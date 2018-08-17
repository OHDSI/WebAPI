package org.ohdsi.webapi.feanalysis.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;

public abstract class BaseFeAnalysisDTOToFeAnalysisConverter<T extends FeAnalysisEntity> extends BaseConversionServiceAwareConverter<FeAnalysisDTO, T> {

    @Override
    public T convert(final FeAnalysisDTO source) {
        final T result = createResultObject();
        
        result.setId(source.getId());
        result.setDescr(source.getDescription());
        result.setDomain(source.getDomain());
        result.setName(source.getName());
        result.setType(source.getType());
        result.setValue(source.getValue());
        
        return result;
    }
}
