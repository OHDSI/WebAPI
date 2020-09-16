package org.ohdsi.webapi.util;

import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;

import static org.ohdsi.webapi.source.SourceDaimon.DaimonType.CDM;
import static org.ohdsi.webapi.source.SourceDaimon.DaimonType.Results;
import static org.ohdsi.webapi.source.SourceDaimon.DaimonType.Temp;
import static org.ohdsi.webapi.source.SourceDaimon.DaimonType.Vocabulary;

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

    public static String getVocabQualifierOrNull(Source source) {
        return source.getTableQualifierOrNull(Vocabulary);
    }

    public static boolean hasSourceDaimon(Source source, SourceDaimon.DaimonType daimonType) {

        boolean result = source.getDaimons().stream().anyMatch(d -> daimonType.equals(d.getDaimonType()));
        if (!result && Vocabulary.equals(daimonType)) {
            result = hasSourceDaimon(source, CDM);
        }
        return result;
    }

}
