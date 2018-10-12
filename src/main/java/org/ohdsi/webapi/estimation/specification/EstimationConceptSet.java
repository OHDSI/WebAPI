package org.ohdsi.webapi.estimation.specification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EstimationConceptSet extends ConceptSet {
    
}
