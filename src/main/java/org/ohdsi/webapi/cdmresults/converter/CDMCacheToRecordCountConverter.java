package org.ohdsi.webapi.cdmresults.converter;

import org.ohdsi.webapi.cdmresults.DescendantRecordCount;
import org.springframework.stereotype.Component;

@Component
public class CDMCacheToRecordCountConverter
        extends BaseCDMCacheToRecordCountConverter<DescendantRecordCount> {
    protected DescendantRecordCount getResultObject() {
        return new DescendantRecordCount();
    }
}
