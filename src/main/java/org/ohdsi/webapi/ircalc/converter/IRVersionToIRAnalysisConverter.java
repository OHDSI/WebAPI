package org.ohdsi.webapi.ircalc.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisDetails;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExportExpression;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.IRVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;

@Component
public class IRVersionToIRAnalysisConverter
        extends BaseConversionServiceAwareConverter<IRVersion, IncidenceRateAnalysis> {
    private static final Logger log = LoggerFactory.getLogger(IRVersionToIRAnalysisConverter.class);

    @Autowired
    private IncidenceRateAnalysisRepository analysisRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CohortDefinitionService cohortService;

    @Override
    public IncidenceRateAnalysis convert(IRVersion source) {
        IncidenceRateAnalysis def = this.analysisRepository.findOne(source.getAssetId().intValue());
        ExceptionUtils.throwNotFoundExceptionIfNull(def,
                String.format("There is no incidence rate analysis with id = %d.", source.getAssetId()));

        IncidenceRateAnalysis target = new IncidenceRateAnalysis();
        target.setId(def.getId());
        target.setTags(def.getTags());
        target.setName(def.getName());
        target.setDescription(source.getDescription());
        target.setTags(def.getTags());
        target.setCreatedBy(def.getCreatedBy());
        target.setCreatedDate(def.getCreatedDate());
        target.setModifiedBy(def.getModifiedBy());
        target.setModifiedDate(def.getModifiedDate());
        target.setExecutionInfoList(def.getExecutionInfoList());

        IncidenceRateAnalysisDetails details = new IncidenceRateAnalysisDetails(target);
        try {
            IncidenceRateAnalysisExportExpression expression = objectMapper.readValue(
                    source.getAssetJson(), IncidenceRateAnalysisExportExpression.class);
            expression.outcomeCohorts = cohortService.getCohortDTOs(expression.outcomeIds);
            expression.targetCohorts = cohortService.getCohortDTOs(expression.targetIds);
            if (expression.outcomeCohorts.size() != expression.outcomeIds.size() ||
                    expression.targetCohorts.size() != expression.targetIds.size()) {
                throw new BadRequestException("Could not restore. Version contains absent cohorts");
            }
        } catch (JsonProcessingException e) {
            log.error("Error converting expression to object", e);
            throw new InternalServerErrorException();
        }
        details.setExpression(source.getAssetJson());

        target.setDetails(details);

        return target;
    }
}
