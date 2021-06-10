package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.dto.CohortGenerationInfoDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortGenerationInfoToCohortGenerationInfoDTOConverter extends BaseConversionServiceAwareConverter<CohortGenerationInfo, CohortGenerationInfoDTO> {
    @Override
    public CohortGenerationInfoDTO convert(CohortGenerationInfo info) {
        final CohortGenerationInfoDTO dto = new CohortGenerationInfoDTO();

        dto.setIsCanceled(info.isCanceled());
        dto.setCreatedBy(conversionService.convert(info.getCreatedBy(), UserDTO.class));
        dto.setExecutionDuration(info.getExecutionDuration());
        dto.setFailMessage(info.getFailMessage());
        dto.setId(info.getId());
        dto.setPersonCount(info.getPersonCount());
        dto.setRecordCount(info.getRecordCount());
        dto.setStartTime(info.getStartTime());
        dto.setStatus(info.getStatus());
        dto.setIsValid(info.isIsValid());

        return dto;
    }
}
