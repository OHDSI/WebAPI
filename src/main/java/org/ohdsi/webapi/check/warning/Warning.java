package org.ohdsi.webapi.check.warning;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DefaultWarning.class, name = "DefaultWarning"),
        @JsonSubTypes.Type(value = ConceptSetWarning.class, name = "ConceptSetWarning")
})
public interface Warning {
    @JsonProperty("message")
    String toMessage();

    @JsonProperty("severity")
    WarningSeverity getSeverity();
}
