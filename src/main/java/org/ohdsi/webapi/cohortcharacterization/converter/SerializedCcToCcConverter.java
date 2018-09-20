package org.ohdsi.webapi.cohortcharacterization.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.springframework.core.convert.ConversionService;

import javax.persistence.AttributeConverter;

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

        TypeReference<CcExportDTO> typeRef = new TypeReference<CcExportDTO>() {};
        CcExportDTO dto = new CcExportDTO();
        try {
            dto = mapper.readValue(data, typeRef);
        } catch (NullPointerException ex) {
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return conversionService.convert(dto, CohortCharacterizationEntity.class);
    }
}
