package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.model.CommonEntityExt;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.service.dto.CommonEntityExtDTO;

import static org.ohdsi.webapi.util.ConversionUtils.convertMetadataDTOExt;

public abstract class BaseCommonDTOToEntityConverter<S extends CommonEntityDTO, T extends CommonEntity<? extends Number>>
        extends BaseConversionServiceAwareConverter<S, T> {
    protected abstract void doConvert(S source, T target);

    @Override
    public T convert(S s) {
        T target = createResultObject(s);
        doConvert(s, target);
        return target;
    }
}
