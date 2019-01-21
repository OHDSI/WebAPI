package org.ohdsi.webapi.prediction.specification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

/**
 *
 * @author asena5
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class PredictionConceptSet extends ConceptSet {
    
}
