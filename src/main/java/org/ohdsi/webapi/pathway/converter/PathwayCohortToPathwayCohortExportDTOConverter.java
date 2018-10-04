package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.pathway.domain.PathwayCohort;
import org.ohdsi.webapi.pathway.dto.PathwayCohortExportDTO;
import org.springframework.stereotype.Component;

@Component
public class PathwayCohortToPathwayCohortExportDTOConverter extends BasePathwayCohortToPathwayCohortDTOConverter<PathwayCohortExportDTO> {

    @Override
    public PathwayCohortExportDTO convert(PathwayCohort source) {

        PathwayCohortExportDTO dto = super.convert(source);
        dto.setExpressionType(source.getCohortDefinition().getExpressionType());
        dto.setExpression(source.getCohortDefinition().getExpression());
        return dto;
    }

    @Override
    protected PathwayCohortExportDTO getResultObject() {

        return new PathwayCohortExportDTO();
    }
}
