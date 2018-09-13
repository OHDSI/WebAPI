package org.ohdsi.webapi.cohortcharacterization.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.springframework.core.convert.ConversionService;

import javax.persistence.AttributeConverter;
import java.io.IOException;

public class SerializedCcToCcConverter implements AttributeConverter<CohortCharacterizationEntity, String> {

    private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static ConversionService conversionService;

    public static void setConversionService(ConversionService conversionService) {

        SerializedCcToCcConverter.conversionService = conversionService;
    }

    @Override
    public String convertToDatabaseColumn(CohortCharacterizationEntity data) {

        String value = "";
        try {
            CcExportDTO cohortCharacterizationDTO = conversionService.convert(data, CcExportDTO.class);
            value = mapper.writeValueAsString(cohortCharacterizationDTO);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public CohortCharacterizationEntity convertToEntityAttribute(String data) {

        CohortCharacterizationEntity cc = new CohortCharacterizationEntity();
        TypeReference<CcExportDTO> typeRef = new TypeReference<CcExportDTO>() {};
        try {
            cc = conversionService.convert(mapper.readValue(data, typeRef), CohortCharacterizationEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cc;
    }
}
