package org.ohdsi.webapi.service.converters;

import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.exception.BadRequestAtlasException;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CohortVersionToCohortDefinitionConverter
        extends BaseConversionServiceAwareConverter<CohortVersion, CohortDefinition> {
    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;

    @Autowired
    private ConceptSetService conceptSetService;

    @Autowired
    private ConceptSetRepository conceptSetRepository;

    @Override
    public CohortDefinition convert(CohortVersion source) {
        CohortDefinition def = this.cohortDefinitionRepository.findOneWithDetail(source.getAssetId());
        ExceptionUtils.throwNotFoundExceptionIfNull(def,
                String.format("There is no cohort definition with id = %d.", source.getAssetId()));

        CohortDefinitionDetails details = new CohortDefinitionDetails();
        details.setExpression(source.getAssetJson());

        CohortDefinition target = new CohortDefinition();
        target.setCohortAnalysisGenerationInfoList(def.getCohortAnalysisGenerationInfoList());
        target.setId(def.getId());
        target.setTags(def.getTags());
        target.setName(def.getName());
        target.setDescription(source.getDescription());
        target.setExpressionType(def.getExpressionType());
        target.setDetails(details);
        target.setCohortAnalysisGenerationInfoList(def.getCohortAnalysisGenerationInfoList());
        target.setGenerationInfoList(def.getGenerationInfoList());
        target.setCohortCharacterizations(def.getCohortCharacterizations());
        target.setTags(def.getTags());
        target.setCreatedBy(def.getCreatedBy());
        target.setCreatedDate(def.getCreatedDate());
        target.setModifiedBy(def.getModifiedBy());
        target.setModifiedDate(def.getModifiedDate());

        CohortExpression expression = CohortExpression.fromJson(def.getDetails().getExpression());
        if (Objects.nonNull(expression.conceptSets)) {
            List<ConceptSet> absentSets = new ArrayList<>();
            for (int i = 0; i < expression.conceptSets.length; i++) {
                ConceptSet conceptSet = expression.conceptSets[i];
                org.ohdsi.webapi.conceptset.ConceptSet existingConceptSet = conceptSetRepository.findById(conceptSet.id);
                if (Objects.nonNull(existingConceptSet)) {
                    conceptSet.expression = conceptSetService.getConceptSetExpression(conceptSet.id);
                    conceptSet.name = conceptSetService.getConceptSet(conceptSet.id).getName();
                } else {
                    absentSets.add(conceptSet);
                }
            }
            if (!absentSets.isEmpty()) {
                String sets = absentSets.stream()
                        .map(c -> String.format("%s (%d)", c.name, c.id))
                        .collect(Collectors.joining("], [", "[", "]"));
                String prefix = absentSets.size() == 1 ? "Concept Set " : "Concept Sets ";
                throw new BadRequestAtlasException(prefix + sets + " are absent");
            }
        }
        String expStr = Utils.serialize(expression);
        target.getDetails().setExpression(expStr);

        return target;
    }
}
