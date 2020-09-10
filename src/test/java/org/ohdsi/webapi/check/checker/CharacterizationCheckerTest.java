package org.ohdsi.webapi.check.checker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.checker.characterization.CharacterizationChecker;
import org.ohdsi.webapi.check.warning.Warning;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;

import java.io.IOException;
import java.util.Optional;

public class CharacterizationCheckerTest extends BaseCheckerTest {
    private static final String JSON_VALID = "/check/checker/characterization-valid.json";
    private static final String JSON_NO_COHORTS = "/check/checker/characterization-no-cohorts.json";
    private static final String JSON_NO_FEATURE_ANALYSES = "/check/checker/characterization-no-fa.json";
    private CharacterizationChecker checker;

    @Before
    public void setUp() {

        checker = new CharacterizationChecker();
        checker.init();
    }

    @Test
    public void checkValid() throws IOException {

        String json = getJsonFromFile(JSON_VALID);
        CohortCharacterizationDTO dto = Utils.deserialize(json, CohortCharacterizationDTO.class);

        Checker<CohortCharacterizationDTO> checker = this.checker;
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertEquals(0, result.getWarnings().size());
    }

    @Test
    public void checkNoCohorts() throws IOException {

        String json = getJsonFromFile(JSON_NO_COHORTS);
        CohortCharacterizationDTO dto = Utils.deserialize(json, CohortCharacterizationDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "cohorts - null or empty");
    }

    @Test
    public void checkNoFeatureAnalyses() throws IOException {

        String json = getJsonFromFile(JSON_NO_FEATURE_ANALYSES);
        CohortCharacterizationDTO dto = Utils.deserialize(json, CohortCharacterizationDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "feature analyses - null or empty");
    }
}
