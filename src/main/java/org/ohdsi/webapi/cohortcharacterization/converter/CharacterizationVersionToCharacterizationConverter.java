package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortcharacterization.specification.CohortCharacterizationImpl;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.versioning.domain.CharacterizationVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CharacterizationVersionToCharacterizationConverter
        extends BaseConversionServiceAwareConverter<CharacterizationVersion, CohortCharacterizationImpl> {
    @Autowired
    private CohortDefinitionService cohortService;

    @Override
    public CohortCharacterizationImpl convert(CharacterizationVersion source) {
        CohortCharacterizationImpl target =
                Utils.deserialize(source.getAssetJson(), CohortCharacterizationImpl.class);

        List<Integer> ids = target.getCohorts().stream()
                .map(CohortMetadataDTO::getId)
                .collect(Collectors.toList());
        List<CohortDTO> cohorts = cohortService.getCohortDTOs(ids);
        if (cohorts.size() != ids.size()) {
            throw new BadRequestException("Could not restore. Version contains absent cohorts");
        }
        target.setCohorts(cohorts);

        return target;
    }
}
