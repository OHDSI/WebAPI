package org.ohdsi.webapi.cdmresults.converter;

import org.ohdsi.webapi.cdmresults.DescendantRecordCount;
import org.ohdsi.webapi.cdmresults.domain.CDMCacheEntity;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;

public abstract class BaseCDMCacheToRecordCountConverter<T extends DescendantRecordCount>
        extends BaseConversionServiceAwareConverter<CDMCacheEntity, T> {
    @Override
    public T convert(CDMCacheEntity s) {
        T target = getResultObject();
        target.setId(s.getConceptId());
        target.setRecordCount(s.getRecordCount());
        target.setDescendantRecordCount(s.getDescendantRecordCount());
        return target;
    }

    protected abstract T getResultObject();
}
