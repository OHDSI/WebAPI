package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author fdefalco
 */
public class Domain {
    
    @JsonProperty("DOMAIN_NAME")    
    public String domainName;
    
    @JsonProperty("DOMAIN_ID")
    public String domainId;
    
    @JsonProperty("DOMAIN_CONCEPT_ID")
    public long domainConceptId;
}
