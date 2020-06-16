package org.ohdsi.webapi.check.checker;

import org.junit.Assert;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.checker.characterization.CharacterizationChecker;
import org.ohdsi.webapi.check.checker.prediction.PredictionChecker;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;

import java.io.IOException;

import static org.junit.Assert.*;

public class PredictionCheckerTest extends BaseCheckerTest {
    private static final String JSON_VALID = "/check/checker/prediction-valid.json";
    private static final String JSON_INVALID = "/check/checker/prediction-invalid.json";

    @Test
    public void checkValid() throws IOException {
        String json = getJsonFromFile(JSON_VALID);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        Checker<PredictionAnalysisDTO> checker = new PredictionChecker();
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertEquals(0, result.getWarnings().size());
    }

    @Test
    public void checkInvalid() throws IOException {
        String json = getJsonFromFile(JSON_INVALID);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        Checker<PredictionAnalysisDTO> checker = new PredictionChecker();
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertNotEquals(0, result.getWarnings().size());
    }
}