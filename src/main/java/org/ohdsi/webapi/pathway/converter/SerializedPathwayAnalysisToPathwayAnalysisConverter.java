package org.ohdsi.webapi.pathway.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.springframework.core.convert.ConversionService;

import javax.persistence.AttributeConverter;

public class SerializedPathwayAnalysisToPathwayAnalysisConverter implements AttributeConverter<PathwayAnalysisEntity, String> {

    private static ConversionService conversionService;

    public static void setConversionService(ConversionService cs) {

        conversionService = cs;
    }

    @Override
    public String convertToDatabaseColumn(PathwayAnalysisEntity data) {

        PathwayAnalysisExportDTO exportDTO = conversionService.convert(data, PathwayAnalysisExportDTO.class);
        return Utils.serialize(exportDTO);
    }

    @Override
    public PathwayAnalysisEntity convertToEntityAttribute(String data) {

        TypeReference<PathwayAnalysisExportDTO> typeRef = new TypeReference<PathwayAnalysisExportDTO>() {};
        return conversionService.convert(Utils.deserialize(data, typeRef), PathwayAnalysisEntity.class);
    }
}
