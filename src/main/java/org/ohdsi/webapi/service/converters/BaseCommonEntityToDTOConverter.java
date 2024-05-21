package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;

import static org.ohdsi.webapi.util.ConversionUtils.convertMetadata;

public abstract class BaseCommonEntityToDTOConverter<S extends CommonEntity<? extends Number>, T extends CommonEntityDTO>
        extends BaseConversionServiceAwareConverter<S, T> {
    protected abstract void doConvert(S source, T target);

    @Override
    public T convert(S s) {
        T target = createResultObject(s);
        convertMetadata(conversionService, s, target);
        doConvert(s, target);
        return target;
    }
}
