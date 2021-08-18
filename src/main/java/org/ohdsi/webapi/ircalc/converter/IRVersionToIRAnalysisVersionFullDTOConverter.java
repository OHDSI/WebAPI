package org.ohdsi.webapi.ircalc.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.exception.ConversionAtlasException;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisDetails;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExportExpression;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.ircalc.dto.IRVersionFullDTO;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.IRVersion;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.InternalServerErrorException;

@Component
public class IRVersionToIRAnalysisVersionFullDTOConverter
        extends BaseConversionServiceAwareConverter<IRVersion, IRVersionFullDTO> {
    private static final Logger log = LoggerFactory.getLogger(IRVersionToIRAnalysisVersionFullDTOConverter.class);

    @Autowired
    private IncidenceRateAnalysisRepository analysisRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CohortDefinitionService cohortService;

    @Override
    public IRVersionFullDTO convert(IRVersion source) {
        IncidenceRateAnalysis def = this.analysisRepository.findOne(source.getAssetId().intValue());
        ExceptionUtils.throwNotFoundExceptionIfNull(def,
                String.format("There is no incidence rate analysis with id = %d.", source.getAssetId()));

        IncidenceRateAnalysis entity = new IncidenceRateAnalysis();
        entity.setId(def.getId());
        entity.setTags(def.getTags());
        entity.setName(def.getName());
        entity.setDescription(source.getDescription());
        entity.setCreatedBy(def.getCreatedBy());
        entity.setCreatedDate(def.getCreatedDate());
        entity.setModifiedBy(def.getModifiedBy());
        entity.setModifiedDate(def.getModifiedDate());
        entity.setExecutionInfoList(def.getExecutionInfoList());

        IncidenceRateAnalysisDetails details = new IncidenceRateAnalysisDetails(entity);
        try {
            IncidenceRateAnalysisExportExpression expression = objectMapper.readValue(
                    source.getAssetJson(), IncidenceRateAnalysisExportExpression.class);
            expression.outcomeCohorts = cohortService.getCohortDTOs(expression.outcomeIds);
            expression.targetCohorts = cohortService.getCohortDTOs(expression.targetIds);
            if (expression.outcomeCohorts.size() != expression.outcomeIds.size() ||
                    expression.targetCohorts.size() != expression.targetIds.size()) {
                throw new ConversionAtlasException("Could not load version because it contains deleted cohorts");
            }
        } catch (JsonProcessingException e) {
            log.error("Error converting expression to object", e);
            throw new InternalServerErrorException();
        }
        details.setExpression(source.getAssetJson());

        entity.setDetails(details);

        IRVersionFullDTO target = new IRVersionFullDTO();
        target.setVersionDTO(conversionService.convert(source, VersionDTO.class));
        target.setEntityDTO(conversionService.convert(entity, IRAnalysisDTO.class));

        return target;
    }
}
