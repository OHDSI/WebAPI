package org.ohdsi.webapi.estimation.specification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

/**
 *
 * @author asena5
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class EstimationConceptSet extends ConceptSet {
    
}
