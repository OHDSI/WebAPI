package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.exception.ConversionAtlasException;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayCohort;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;
import org.ohdsi.webapi.pathway.dto.PathwayVersionFullDTO;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.PathwayVersion;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PathwayVersionToPathwayVersionFullDTOConverter
        extends BaseConversionServiceAwareConverter<PathwayVersion, PathwayVersionFullDTO> {
    @Autowired
    private PathwayAnalysisEntityRepository analysisRepository;

    @Autowired
    private CohortDefinitionService cohortService;

    @Override
    public PathwayVersionFullDTO convert(PathwayVersion source) {
        PathwayAnalysisEntity def = this.analysisRepository.findOne(source.getAssetId().intValue());
        ExceptionUtils.throwNotFoundExceptionIfNull(def,
                String.format("There is no pathway analysis with id = %d.", source.getAssetId()));

        PathwayAnalysisExportDTO exportDTO = Utils.deserialize(source.getAssetJson(), PathwayAnalysisExportDTO.class);

        PathwayAnalysisEntity entity = conversionService.convert(exportDTO, PathwayAnalysisEntity.class);
        entity.setId(def.getId());
        entity.setTags(def.getTags());
        entity.setName(def.getName());
        entity.setCreatedBy(def.getCreatedBy());
        entity.setCreatedDate(def.getCreatedDate());
        entity.setModifiedBy(def.getModifiedBy());
        entity.setModifiedDate(def.getModifiedDate());

        entity.setEventCohorts(getCohorts(entity.getEventCohorts(), PathwayEventCohort.class));
        entity.setTargetCohorts(getCohorts(entity.getTargetCohorts(), PathwayTargetCohort.class));

        PathwayVersionFullDTO target = new PathwayVersionFullDTO();
        target.setVersionDTO(conversionService.convert(source, VersionDTO.class));
        target.setEntityDTO(conversionService.convert(entity, PathwayAnalysisDTO.class));

        return target;
    }

    private <T extends PathwayCohort> Set<T> getCohorts(Set<T> pathwayCohorts, Class<T> clazz) {
        List<Integer> cohortIds =
                pathwayCohorts.stream()
                        .map(c -> c.getCohortDefinition().getId())
                        .collect(Collectors.toList());

        List<CohortDefinition> cohorts = cohortService.getCohorts(cohortIds);
        if (cohorts.size() != cohortIds.size()) {
            throw new ConversionAtlasException("Could not load version because it contains deleted cohorts");
        }
        return cohorts.stream()
                .map(c -> conversionService.convert(c, PathwayCohortDTO.class))
                .map(c -> conversionService.convert(c, clazz))
                .collect(Collectors.toSet());
    }
}
