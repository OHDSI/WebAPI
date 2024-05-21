package org.ohdsi.webapi.cdmresults.converter;

import org.ohdsi.webapi.cdmresults.DescendantRecordAndPersonCount;
import org.ohdsi.webapi.cdmresults.domain.CDMCacheEntity;
import org.springframework.stereotype.Component;

@Component
public class PersonCountToCDMCacheConverter extends RecordCountToCDMCacheConverter<DescendantRecordAndPersonCount> {
    @Override
    public CDMCacheEntity convert(DescendantRecordAndPersonCount s) {
        CDMCacheEntity target = super.convert(s);
        target.setPersonCount(s.getPersonCount());
        target.setDescendantPersonCount(s.getDescendantPersonCount());
        return target;
    }
}
