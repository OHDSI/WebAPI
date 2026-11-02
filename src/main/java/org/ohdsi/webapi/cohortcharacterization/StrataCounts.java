package org.ohdsi.webapi.cohortcharacterization;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Resolves the number of people per cohort and strata (strata 0 = overall) from candidate values derived
 * from stored results.
 */
public final class StrataCounts {

    /** A derived N for a cohort/strata and the number of result rows that produced it. */
    public record Candidate(long cohortId, long strataId, long personCount, long rowCount) {
    }

    private StrataCounts() {
    }

    /**
     * @return cohortId -> strataId -> N, keyed by string ids for JSON. When rows disagree, the value supported
     * by the most rows wins (ties go to the larger N).
     */
    public static Map<String, Map<String, Long>> resolve(Collection<Candidate> candidates) {
        Map<Long, Map<Long, Candidate>> best = new TreeMap<>();
        Comparator<Candidate> preference = Comparator.comparingLong(Candidate::rowCount)
                .thenComparingLong(Candidate::personCount);
        for (Candidate c : candidates) {
            best.computeIfAbsent(c.cohortId(), k -> new TreeMap<>())
                    .merge(c.strataId(), c, (a, b) -> preference.compare(a, b) >= 0 ? a : b);
        }
        Map<String, Map<String, Long>> result = new LinkedHashMap<>();
        best.forEach((cohortId, byStrata) -> result.put(String.valueOf(cohortId), byStrata.entrySet().stream()
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> e.getValue().personCount(),
                        (a, b) -> a, LinkedHashMap::new))));
        return result;
    }
}
