package org.ohdsi.webapi.check.warning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

import java.util.Objects;

public class ConceptSetWarning extends BaseWarning implements Warning {

    private final String template;
    private final ConceptSet conceptSet;

    public ConceptSetWarning(WarningSeverity severity, String template, ConceptSet conceptSet) {

        super(severity);
        this.template = template;
        this.conceptSet = conceptSet;
    }

    @JsonIgnore
    public ConceptSet getConceptSet() {

        return conceptSet;
    }

    @JsonProperty("conceptSetId")
    public Integer getConceptSetId() {
        return Objects.nonNull(conceptSet) ? conceptSet.id : 0;
    }

    @Override
    public String toMessage() {

        return String.format(template, conceptSet.name);
    }
}
