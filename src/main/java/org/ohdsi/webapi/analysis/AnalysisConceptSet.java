package org.ohdsi.webapi.analysis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

@JsonIgnoreProperties(ignoreUnknown=true)
public class AnalysisConceptSet extends ConceptSet {
    
}
