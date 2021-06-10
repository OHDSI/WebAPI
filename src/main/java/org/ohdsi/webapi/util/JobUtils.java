package org.ohdsi.webapi.util;

import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.CDM_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.RESULTS_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.TARGET_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.TEMP_DATABASE_SCHEMA;

public final class JobUtils {
    
    private JobUtils(){}

    public static String getSchema(Map<String, Object> jobParams, String qualifier){
        if (jobParams.get(qualifier) != null){
            return jobParams.get(qualifier).toString();
        } else {
            String daimonName = "";
            switch (qualifier){
                case RESULTS_DATABASE_SCHEMA:
                case TARGET_DATABASE_SCHEMA: 
                    daimonName = "Results"; 
                    break;
                case CDM_DATABASE_SCHEMA:
                    daimonName = "CDM";
                    break;
                case TEMP_DATABASE_SCHEMA:
                    daimonName = "Temp";
                    break;
            }
            throw new NullPointerException(String.format("DaimonType (\"%s\") not found in Source", daimonName));
        }
    }
}
