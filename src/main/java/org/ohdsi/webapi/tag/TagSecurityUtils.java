package org.ohdsi.webapi.tag;

import org.apache.shiro.SecurityUtils;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.model.CommonEntityExt;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.reusable.domain.Reusable;

import javax.ws.rs.BadRequestException;

public class TagSecurityUtils {
    public static String COHORT_DEFINITION = "cohortdefinition";
    public static String CONCEPT_SET = "conceptset";
    public static String COHORT_CHARACTERIZATION = "cohort-characterization";
    public static String INCIDENCE_RATE = "ir";
    public static String PATHWAY_ANALYSIS = "pathway-analysis";
    public static String REUSABLE = "reusable";

    public static boolean canAssingProtectedTags() {
        return checkPermission(COHORT_DEFINITION, "post") ||
                checkPermission(CONCEPT_SET, "post") ||
                checkPermission(COHORT_CHARACTERIZATION, "post") ||
                checkPermission(INCIDENCE_RATE, "post") ||
                checkPermission(PATHWAY_ANALYSIS, "post") ||
                checkPermission(REUSABLE, "post");
    }

    public static boolean canUnassingProtectedTags() {
        return checkPermission(COHORT_DEFINITION, "delete") ||
                checkPermission(CONCEPT_SET, "delete") ||
                checkPermission(COHORT_CHARACTERIZATION, "delete") ||
                checkPermission(INCIDENCE_RATE, "delete") ||
                checkPermission(PATHWAY_ANALYSIS, "delete") ||
                checkPermission(REUSABLE, "delete");
    }

    public static boolean checkPermission(final String asset, final String method) {
        if (asset == null) {
            return false;
        }

        final String template;
        switch (method) {
            case "post":
                template = "%s:*:protectedtag:post";
                break;
            case "delete":
                template = "%s:*:protectedtag:*:delete";
                break;
            default:
                throw new BadRequestException(String.format("Unsupported method: %s", method));

        }
        final String permission = String.format(template, asset);
        return SecurityUtils.getSubject().isPermitted(permission);
    }

    public static String getAssetName(final CommonEntityExt<?> entity) {
        if (entity instanceof ConceptSet) {
            return TagSecurityUtils.CONCEPT_SET;
        } else if (entity instanceof CohortDefinition) {
            return TagSecurityUtils.COHORT_DEFINITION;
        } else if (entity instanceof CohortCharacterizationEntity) {
            return TagSecurityUtils.COHORT_CHARACTERIZATION;
        } else if (entity instanceof IncidenceRateAnalysis) {
            return TagSecurityUtils.INCIDENCE_RATE;
        } else if (entity instanceof PathwayAnalysisEntity) {
            return TagSecurityUtils.PATHWAY_ANALYSIS;
        } else if (entity instanceof Reusable) {
            return TagSecurityUtils.REUSABLE;
        }
        return null;
    }
}
