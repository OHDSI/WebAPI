package org.ohdsi.webapi.feanalysis;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.type.CollectionType;
import org.ohdsi.analysis.cohortcharacterization.design.CcResultType;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.webapi.feanalysis.dto.BaseFeAnalysisCriteriaDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisAggregateDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisWithConceptSetDTO;
import org.springframework.beans.factory.annotation.Autowired;

public class FeAnalysisDeserializer extends JsonDeserializer<FeAnalysisDTO> {

    @Autowired
    private ObjectMapper objectMapper;
    
    // need to look around and find a way to override procedure of base mapping
    // and handle only a design field
    
    @Override
    public FeAnalysisDTO deserialize(final JsonParser parser, final DeserializationContext ctxt) throws IOException, JsonProcessingException {

        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);

        FeAnalysisDTO dto = createDto(node);

        final JsonNode name = node.get("name");
        if (name != null && !name.isNull()) {
            dto.setName(name.textValue());    
        }

        final JsonNode description = node.get("description");
        if (description != null && !name.isNull()) {
            dto.setDescription(description.textValue());    
        }

        final JsonNode descr = node.get("descr");
        if (descr != null && !descr.isNull()) {
            dto.setDescription(descr.textValue());
        }

        final JsonNode id = node.get("id");
        if (id != null && !id.isNull()) {
            dto.setId(id.intValue());
        }

        final JsonNode domain = node.get("domain");
        if (domain != null && !domain.isNull()) {
            final String domainString = domain.textValue();
            dto.setDomain(StandardFeatureAnalysisDomain.valueOf(domainString));
        }

        final StandardFeatureAnalysisType analysisType = getType(node);
        if (analysisType != null) {
            dto.setType(analysisType);

            final JsonNode design = node.get("design");
            if (analysisType == StandardFeatureAnalysisType.CRITERIA_SET) {
                JsonNode statType = node.get("statType");
                if (statType != null) {
                    dto.setStatType(CcResultType.valueOf(statType.textValue()));
                }
                final List<BaseFeAnalysisCriteriaDTO> list = new ArrayList<>();
                for (final JsonNode jsonNode : design) {
                    list.add(convert(jsonNode));
                }
                dto.setDesign(list);

                final JsonNode conceptSets = node.get("conceptSets");
                if (Objects.nonNull(conceptSets)) {
                    CollectionType typeRef = objectMapper.getTypeFactory().constructCollectionType(List.class, ConceptSet.class);
                    List<ConceptSet> conceptSetList = objectMapper.readValue(conceptSets.traverse(), typeRef);
                    ((FeAnalysisWithConceptSetDTO)dto).setConceptSets(conceptSetList);
                }
            } else {
                dto.setDesign(design.textValue());
            }
        }

        return dto;
    }

    private StandardFeatureAnalysisType getType(JsonNode jsonNode) {
        final JsonNode type = jsonNode.get("type");
        StandardFeatureAnalysisType result = null;
        if (Objects.nonNull(type) && !type.isNull()) {
            result = StandardFeatureAnalysisType.valueOf(type.textValue());
        }
        return result;
    }

    private FeAnalysisDTO createDto(JsonNode jsonNode) {
        final StandardFeatureAnalysisType type = getType(jsonNode);
        FeAnalysisDTO analysisDTO;
        if (Objects.equals(StandardFeatureAnalysisType.CRITERIA_SET, type)) {
            analysisDTO = new FeAnalysisWithConceptSetDTO();
        } else {
            analysisDTO = new FeAnalysisDTO();
        }
        return analysisDTO;
    }
    
    private BaseFeAnalysisCriteriaDTO convert(final JsonNode node) {
        try {
            return objectMapper.treeToValue(node, BaseFeAnalysisCriteriaDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
