package org.ohdsi.webapi.cohortdefinition.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.service.converters.BaseCommonDTOExtToEntityExtConverter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CohortRawDTOToCohortDTOConverter extends BaseConversionServiceAwareConverter<CohortRawDTO, CohortDTO> {
    @Override
    public CohortDTO convert(CohortRawDTO source) {
        CohortDTO target = new CohortDTO();
        target.setDescription(source.getDescription());
        target.setName((source.getName()));
        target.setId(source.getId());

        CohortExpression expression = Objects.nonNull(source.getExpression()) ?
                Utils.deserialize(source.getExpression(), new TypeReference<CohortExpression>() {}) : null;
        target.setExpression(expression);
        target.setExpressionType(source.getExpressionType());
        target.setCreatedBy(source.getCreatedBy());
        target.setCreatedDate(source.getCreatedDate());
        target.setModifiedBy(source.getModifiedBy());
        target.setModifiedDate(source.getModifiedDate());
        target.setTags(source.getTags());

        return target;
    }
}
