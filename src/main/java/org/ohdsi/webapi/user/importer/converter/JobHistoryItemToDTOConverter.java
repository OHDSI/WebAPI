package org.ohdsi.webapi.user.importer.converter;

import org.ohdsi.webapi.user.importer.dto.JobHistoryItemDTO;
import org.ohdsi.webapi.user.importer.model.UserImportJobHistoryItem;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class JobHistoryItemToDTOConverter implements Converter<UserImportJobHistoryItem, JobHistoryItemDTO> {

  public JobHistoryItemToDTOConverter(GenericConversionService conversionService) {

    conversionService.addConverter(this);
  }

  @Override
  public JobHistoryItemDTO convert(UserImportJobHistoryItem source) {

    JobHistoryItemDTO dto = new JobHistoryItemDTO();
    dto.setId(source.getId());
    dto.setAuthor(source.getAuthor());
    dto.setEndTime(source.getEndTime());
    dto.setExitMessage(source.getExitMessage());
    dto.setJobTitle(source.getJobName());
    dto.setProviderType(source.getUserImport() != null ? source.getUserImport().getProviderType() : null);
    dto.setStartTime(source.getStartTime());
    dto.setStatus(source.getStatus());
    dto.setExitCode(source.getExitCode());
    return dto;
  }
}
