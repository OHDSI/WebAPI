package org.ohdsi.webapi.util;

import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;

public class ConversionUtils {

    public static void convertMetadata(CommonEntity source, CommonEntityDTO target) {
        target.setCreatedBy(UserUtils.nullSafeLogin(source.getCreatedBy()));
        target.setCreatedDate(source.getCreatedDate());
        target.setModifiedBy(UserUtils.nullSafeLogin(source.getModifiedBy()));
        target.setModifiedDate(source.getModifiedDate());
    }
}
