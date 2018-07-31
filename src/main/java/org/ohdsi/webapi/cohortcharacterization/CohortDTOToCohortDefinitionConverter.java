package org.ohdsi.webapi.cohortcharacterization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.PostConstruct;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class CohortDTOToCohortDefinitionConverter extends BaseConversionServiceAwareConverter<CohortDTO, CohortDefinition> {
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    private void init(){
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    
    @Override
    public CohortDefinition convert(final CohortDTO source) {
        final CohortDefinition cohortDefinition = new CohortDefinition();
        
        cohortDefinition.setId(source.getId());
        cohortDefinition.setName(source.getName());
        cohortDefinition.setExpressionType(source.getExpressionType());

        if (source.getExpression() != null) {
            final CohortDefinitionDetails details = new CohortDefinitionDetails();
            final String expression = convertExpression(source);
            details.setExpression(expression);
            details.setHashCode(expression.hashCode());
            cohortDefinition.setDetails(details);
        }
        
        return cohortDefinition;
    }

    private String convertExpression(final CohortDTO source) {
        try {
            return objectMapper.writeValueAsString(source.getExpression());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
