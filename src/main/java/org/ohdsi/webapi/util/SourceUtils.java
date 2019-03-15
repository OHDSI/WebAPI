package org.ohdsi.webapi.util;

import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;

import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.CDM_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.RESULTS_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.TARGET_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Params.TEMP_DATABASE_SCHEMA;
import static org.ohdsi.webapi.source.SourceDaimon.DaimonType.CDM;
import static org.ohdsi.webapi.source.SourceDaimon.DaimonType.Results;
import static org.ohdsi.webapi.source.SourceDaimon.DaimonType.Temp;

public class SourceUtils {
    public static String getVocabularyQualifier(Source source) {
        return source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    }

    public static String getCdmQualifier(Source source) {
        return source.getTableQualifier(CDM);
    }

    public static String getTempQualifier(Source source, String backup) {
        return ObjectUtils.firstNonNull( source.getTableQualifierOrNull(Temp), backup);
    }

    public static String getTempQualifier(Source source) {
        return ObjectUtils.firstNonNull( source.getTableQualifierOrNull(Temp), getResultsQualifier(source));
    }

    public static String getTempQualifierOrNull(Source source) {
        return source.getTableQualifierOrNull(Temp) != null ? source.getTableQualifierOrNull(Temp) : getResultsQualifierOrNull(source);
    }

    public static String getResultsQualifier(Source source) {
        return source.getTableQualifier(Results);
    }

    public static String getResultsQualifierOrNull(Source source) {
        return source.getTableQualifierOrNull(Results);
    }

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
