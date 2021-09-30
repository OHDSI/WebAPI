package org.ohdsi.webapi.cohortdefinition.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CohortExpression;

import java.io.IOException;

public class CohortDTOSerializer extends JsonSerializer<CohortExpression> {

    @Override
    public void serialize(CohortExpression value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeRawValue(Utils.serialize(value));
    }
}
