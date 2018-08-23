package org.ohdsi.webapi.feanalysis;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisCriteriaDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisDTO;

public class FeAnalysisDeserializer extends JsonDeserializer<FeAnalysisDTO> {
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    private void init() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    // need to look around and find a way to override procedure of base mapping
    // and handle only a design field
    
    @Override
    public FeAnalysisDTO deserialize(final JsonParser parser, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        FeAnalysisDTO dto = new FeAnalysisDTO();

        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);

        final JsonNode name = node.get("name");
        if (name != null) {
            dto.setName(name.textValue());    
        }

        final JsonNode description = node.get("description");
        if (description != null) {
            dto.setDescription(description.textValue());    
        }

        final JsonNode id = node.get("id");
        if (id != null) {
            dto.setId(id.longValue());
        }

        final JsonNode domain = node.get("domain");
        if (domain != null) {
            final String domainString = domain.textValue();
            dto.setDomain(StandardFeatureAnalysisDomain.valueOf(domainString));
        }


        final JsonNode type = node.get("type");
        if (type != null) {
            final String typeString = type.textValue();
            final StandardFeatureAnalysisType analysisType = StandardFeatureAnalysisType.valueOf(typeString);
            dto.setType(analysisType);

            final JsonNode design = node.get("design");
            if (analysisType == StandardFeatureAnalysisType.CRITERIA_SET) {
                final List<FeAnalysisCriteriaDTO> list = new ArrayList<>();
                for (final JsonNode jsonNode : design) {
                    list.add(convert(jsonNode));
                }
                dto.setDesign(list);
            } else {
                dto.setDesign(design.textValue());
            }
        }

        return dto;
    }
    
    private FeAnalysisCriteriaDTO convert(final JsonNode node) {
        try {
            return objectMapper.treeToValue(node, FeAnalysisCriteriaDTO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
