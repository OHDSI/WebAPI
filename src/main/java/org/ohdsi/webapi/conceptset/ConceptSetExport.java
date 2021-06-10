/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.conceptset;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

/**
 *
 * @author Anthony Sena <https://github.com/ohdsi>
 */
public class ConceptSetExport {
    @JsonProperty("ConceptSetId")
    public int ConceptSetId;
    
    @JsonProperty("ConceptSetName")
    public String ConceptSetName;
    
    @JsonProperty("ConceptSetExpression")
    public ConceptSetExpression csExpression;
        
    @JsonProperty("IdentifierConcepts")
    public Collection<Concept> identifierConcepts;
    
    @JsonProperty("MappedConcepts")
    public Collection<Concept> mappedConcepts;
}
