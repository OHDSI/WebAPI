package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.analysis.Cohort;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.converter.BaseCohortDTOToCohortDefinitionConverter;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.pathway.domain.PathwayCohort;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.dto.PathwayCohortExportDTO;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

//@Component
public class PathwayCohortExportDTOToCohortConverter extends BaseCohortDTOToCohortDefinitionConverter<PathwayCohortExportDTO> {
    private String convertExpression(final Cohort source) {
        return Utils.serialize(source.getExpression());
    }

    @Override
    protected void doConvert(PathwayCohortExportDTO source, CohortDefinition target) {
        super.doConvert(source, target);
        if (source.getExpression() != null) {
            final CohortDefinitionDetails details = new CohortDefinitionDetails();
            final String expression = convertExpression(target);
            details.setExpression(expression);
            target.setDetails(details);
        }
    }
}
