package org.ohdsi.webapi.feanalysis;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;

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
