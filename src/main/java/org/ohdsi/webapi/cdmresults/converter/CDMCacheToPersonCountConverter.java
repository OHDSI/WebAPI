package org.ohdsi.webapi.cdmresults.converter;

import org.ohdsi.webapi.cdmresults.DescendantRecordAndPersonCount;
import org.ohdsi.webapi.cdmresults.domain.CDMCacheEntity;
import org.springframework.stereotype.Component;

@Component
public class CDMCacheToPersonCountConverter
        extends BaseCDMCacheToRecordCountConverter<DescendantRecordAndPersonCount> {
    @Override
    public DescendantRecordAndPersonCount convert(CDMCacheEntity s) {
        DescendantRecordAndPersonCount target = super.convert(s);
        target.setPersonCount(s.getPersonCount());
        target.setDescendantPersonCount(s.getDescendantPersonCount());
        return target;
    }

    protected DescendantRecordAndPersonCount getResultObject() {
        return new DescendantRecordAndPersonCount();
    }
}
