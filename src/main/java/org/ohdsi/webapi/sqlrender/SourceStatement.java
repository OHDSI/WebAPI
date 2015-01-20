package org.ohdsi.webapi.sqlrender;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

/**
 *
 * @author Lee Evans
 */
public class SourceStatement {
    
    public SourceStatement() {
    
    }
        
    @JsonProperty("targetdialect")
    public String targetDialect;
    
    @JsonProperty("SQL")
    public String sql;
    
    @JsonProperty("parameters")
    public HashMap<String,String> parameters;
     
}
