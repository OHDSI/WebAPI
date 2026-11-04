package org.ohdsi.webapi.mcp;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.ohdsi.circe.vocabulary.Concept;

/** Compact, snake_case views of vocabulary concepts for tool results. */
final class ConceptViews {

    private ConceptViews() {
    }

    static Map<String, Object> view(Concept c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("concept_id", c.conceptId);
        m.put("concept_name", c.conceptName);
        m.put("domain_id", c.domainId);
        m.put("vocabulary_id", c.vocabularyId);
        m.put("concept_class_id", c.conceptClassId);
        m.put("concept_code", c.conceptCode);
        m.put("standard_concept", c.standardConcept);
        m.put("invalid_reason", c.invalidReason);
        return m;
    }

    /** Vocabulary queries report a standard concept as 'S' and a non-standard one as 'N' (or null). */
    static boolean isStandard(Concept c) {
        return "S".equals(c.standardConcept);
    }

    /** Vocabulary queries report a valid concept as 'V' (or null). */
    static boolean isValid(Concept c) {
        return c.invalidReason == null || "V".equals(c.invalidReason);
    }

    /** Case- and whitespace-insensitive name comparison, used to catch concept ids that do not mean what the caller thinks. */
    static boolean sameName(String claimed, String actual) {
        return Objects.equals(normalize(claimed), normalize(actual));
    }

    private static String normalize(String s) {
        return s == null ? null : s.strip().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }
}
