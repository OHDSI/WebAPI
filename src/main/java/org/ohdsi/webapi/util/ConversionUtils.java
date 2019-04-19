package org.ohdsi.webapi.util;

import org.ohdsi.webapi.common.analyses.CommonAnalysisDTO;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.springframework.core.convert.support.GenericConversionService;

public class ConversionUtils {

    public static void convertMetadata(CommonEntity source, CommonEntityDTO target) {
        target.setCreatedBy(UserUtils.nullSafeLogin(source.getCreatedBy()));
        target.setCreatedDate(source.getCreatedDate());
        target.setModifiedBy(UserUtils.nullSafeLogin(source.getModifiedBy()));
        target.setModifiedDate(source.getModifiedDate());
    }

    public static void convertMetadata(GenericConversionService conversionService, CommonEntity source, CommonAnalysisDTO target) {
        target.setCreatedBy(conversionService.convert(source.getCreatedBy(), UserDTO.class));
        target.setCreatedDate(source.getCreatedDate());
        target.setModifiedBy(conversionService.convert(source.getModifiedBy(), UserDTO.class));
        target.setModifiedDate(source.getModifiedDate());
    }
}
