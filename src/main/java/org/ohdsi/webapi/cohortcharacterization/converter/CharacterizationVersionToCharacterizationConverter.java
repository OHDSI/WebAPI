package org.ohdsi.webapi.cohortcharacterization.converter;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ohdsi.analysis.Cohort;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.ohdsi.webapi.cohortcharacterization.specification.CohortCharacterizationImpl;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.CohortService;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.CharacterizationVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CharacterizationVersionToCharacterizationConverter
        extends BaseConversionServiceAwareConverter<CharacterizationVersion, CohortCharacterizationImpl> {
    @Autowired
    private CcRepository repository;

    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;

    @Override
    public CohortCharacterizationImpl convert(CharacterizationVersion source) {
        CohortCharacterizationImpl target =
                Utils.deserialize(source.getAssetJson(), CohortCharacterizationImpl.class);

        if (Objects.nonNull(target.getCohorts())) {
            List<CohortDTO> absentCohorts = new ArrayList<>();
            List<CohortDTO> cohorts = target.getCohorts().stream()
                    .map(c -> {
                        try {
                            CohortDefinition def = cohortDefinitionRepository.findOneWithDetail(c.getId());
                            return conversionService.convert(def, CohortDTO.class);
                        } catch (NotFoundException e) {
                            absentCohorts.add(c);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            target.setCohorts(cohorts);

            if (!absentCohorts.isEmpty()) {
                String prefix = absentCohorts.size() == 1 ? "cohort " : "cohorts ";
                String message = absentCohorts.stream()
                        .map(c -> c.getName() + "(" + c.getId() + ")")
                        .collect(Collectors.joining("], [", "[", "]"));
                throw new BadRequestException("Could not restore. Version contains absent " + prefix + message);
            }
        }

        return target;
    }
}
