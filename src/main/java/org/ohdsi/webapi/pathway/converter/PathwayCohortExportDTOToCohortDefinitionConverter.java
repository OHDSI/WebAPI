package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.analysis.Cohort;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsEntity;
import org.ohdsi.webapi.cohortdefinition.converter.BaseCohortDTOToCohortDefinitionConverter;
import org.ohdsi.webapi.pathway.dto.PathwayCohortExportDTO;
import org.springframework.stereotype.Component;

@Component
public class PathwayCohortExportDTOToCohortDefinitionConverter extends BaseCohortDTOToCohortDefinitionConverter<PathwayCohortExportDTO> {
    private String convertExpression(final Cohort source) {
        return Utils.serialize(source.getExpression());
    }

    @Override
    protected void doConvert(PathwayCohortExportDTO source, CohortDefinitionEntity target) {
        super.doConvert(source, target);
        if (source.getExpression() != null) {
            final CohortDefinitionDetailsEntity details = new CohortDefinitionDetailsEntity();
            final String expression = convertExpression(source);
            details.setExpression(expression);
            target.setDetails(details);
        }
    }
}
