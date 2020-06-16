package org.ohdsi.webapi.check.checker;

import org.junit.Assert;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.checker.characterization.CharacterizationChecker;
import org.ohdsi.webapi.check.checker.estimation.EstimationChecker;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;

import java.io.IOException;

import static org.junit.Assert.*;

public class EstimationCheckerTest extends BaseCheckerTest {
    private static final String JSON_VALID = "/check/checker/estimation-valid.json";
    private static final String JSON_INVALID = "/check/checker/estimation-invalid.json";

    @Test
    public void checkValid() throws IOException {
        String json = getJsonFromFile(JSON_VALID);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        Checker<EstimationDTO> checker = new EstimationChecker();
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertEquals(0, result.getWarnings().size());
    }

    @Test
    public void checkInvalid() throws IOException {
        String json = getJsonFromFile(JSON_INVALID);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        Checker<EstimationDTO> checker = new EstimationChecker();
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertNotEquals(0, result.getWarnings().size());
    }
}