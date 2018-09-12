package org.ohdsi.webapi.pathway.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.springframework.core.convert.ConversionService;

import javax.persistence.AttributeConverter;
import java.io.IOException;

public class SerializedPathwayAnalysisToPathwayAnalysisConverter implements AttributeConverter<PathwayAnalysisEntity, String> {

    private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static ConversionService conversionService;

    // TODO: cleaner way
    public static void setConversionService(ConversionService cs) {

        conversionService = cs;
    }

    @Override
    public String convertToDatabaseColumn(PathwayAnalysisEntity data) {

        String value = "";
        try {
            PathwayAnalysisExportDTO exportDTO = conversionService.convert(data, PathwayAnalysisExportDTO.class);
            value = mapper.writeValueAsString(exportDTO);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public PathwayAnalysisEntity convertToEntityAttribute(String data) {

        PathwayAnalysisEntity cc = new PathwayAnalysisEntity();
        TypeReference<PathwayAnalysisExportDTO> typeRef = new TypeReference<PathwayAnalysisExportDTO>() {};
        try {
            cc = conversionService.convert(mapper.readValue(data, typeRef), PathwayAnalysisEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cc;
    }
}
