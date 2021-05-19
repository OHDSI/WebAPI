package org.ohdsi.webapi.conceptset.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.conceptset.ConceptSetVersionDto;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.ohdsi.webapi.versioning.domain.ConceptSetVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConceptSetVersionToConceptSetVersionDTOConverter
        extends BaseConversionServiceAwareConverter<ConceptSetVersion, ConceptSetVersionDto> {
    @Autowired
    private ConceptSetRepository conceptSetRepository;

    @Override
    public ConceptSetVersionDto convert(ConceptSetVersion source) {
        List<ConceptSetItem> items = Utils.deserialize(source.getAssetJson(), new TypeReference<List<ConceptSetItem>>() {});
        ConceptSet conceptSet = conceptSetRepository.findById(source.getAssetId());
        ExceptionUtils.throwNotFoundExceptionIfNull(conceptSet, String.format("There is no concept set with id = %d.", source.getAssetId()));

        ConceptSetVersionDto target = new ConceptSetVersionDto();
        target.setItems(items);
        target.setName(conceptSet.getName());
        target.setId(conceptSet.getId());
        target.setCreatedBy(conversionService.convert(conceptSet.getCreatedBy(), UserDTO.class));
        target.setCreatedDate(conceptSet.getCreatedDate());
        target.setCreatedBy(conversionService.convert(conceptSet.getModifiedBy(), UserDTO.class));
        target.setCreatedDate(conceptSet.getModifiedDate());

        return target;
    }
}
