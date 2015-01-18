package org.ohdsi.webapi.sqlrender;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Lee Evans
 */
public class TranslatedStatement {
    
    public TranslatedStatement() {

    }
    
    @JsonProperty("targetSQL")
    public String targetSQL;
}
