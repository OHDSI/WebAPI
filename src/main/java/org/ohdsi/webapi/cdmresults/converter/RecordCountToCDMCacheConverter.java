package org.ohdsi.webapi.cdmresults.converter;

import org.ohdsi.webapi.cdmresults.DescendantRecordCount;
import org.ohdsi.webapi.cdmresults.domain.CDMCacheEntity;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class RecordCountToCDMCacheConverter<S extends DescendantRecordCount>
        extends BaseConversionServiceAwareConverter<S, CDMCacheEntity> {
    @Override
    public CDMCacheEntity convert(S s) {
        CDMCacheEntity target = new CDMCacheEntity();
        target.setConceptId(s.getId());
        target.setRecordCount(s.getRecordCount());
        target.setDescendantRecordCount(s.getDescendantRecordCount());
        return target;
    }
}
