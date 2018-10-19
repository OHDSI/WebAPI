package org.ohdsi.webapi.util;

import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;

public class SourceUtils {
    public static String getVocabularyQualifier(Source source) {
        return source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    }

    public static String getCdmQualifier(Source source) {
        return source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    }

    public static String getTempQualifier(Source source, String backup) {
        return ObjectUtils.firstNonNull( source.getTableQualifierOrNull(SourceDaimon.DaimonType.Temp), backup);
    }

    public static String getTempQualifier(Source source) {
        return ObjectUtils.firstNonNull( source.getTableQualifierOrNull(SourceDaimon.DaimonType.Temp), getResultsQualifier(source));
    }

    public static String getResultsQualifier(Source source) {
        return source.getTableQualifier(SourceDaimon.DaimonType.Results);
    }

}
