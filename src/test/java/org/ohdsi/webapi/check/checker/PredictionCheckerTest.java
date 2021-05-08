package org.ohdsi.webapi.check.checker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.checker.characterization.CharacterizationChecker;
import org.ohdsi.webapi.check.checker.pathway.PathwayChecker;
import org.ohdsi.webapi.check.checker.prediction.PredictionChecker;
import org.ohdsi.webapi.check.warning.Warning;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

public class PredictionCheckerTest extends BaseCheckerTest {
    private static final String JSON_VALID = "/check/checker/prediction-valid.json";
    private static final String JSON_INVALID_VALUES = "/check/checker/prediction-invalid-values.json";
    private static final String JSON_NO_VALUES = "/check/checker/prediction-no-values.json";
    @Autowired
    private PredictionChecker checker;

    @Test
    public void checkValid() throws IOException {

        String json = getJsonFromFile(JSON_VALID);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        Checker<PredictionAnalysisDTO> checker = this.checker;
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertEquals(0, result.getWarnings().size());
    }

    @Test
    public void checkNoSpecification() throws IOException {

        String json = getJsonFromFile(JSON_VALID);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification - null or empty");
    }

    @Test
    public void checkNoRunPplArgs() throws IOException {

        String json = getJsonFromFile(JSON_NO_VALUES);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: settings - null or empty");
    }

    @Test
    public void checkInvalidRunPplArgsTestFraction() throws IOException {

        String json = getJsonFromFile(JSON_INVALID_VALUES);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: settings :: test fraction - must be between 0 and 100");
    }

    @Test
    public void checkInvalidRunPplArgsMinFraction() throws IOException {

        String json = getJsonFromFile(JSON_INVALID_VALUES);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: settings :: minimum covariate fraction - must be greater or equal to 0");
    }

    @Test
    public void checkNoOutcomeCohorts() throws IOException {

        String json = getJsonFromFile(JSON_NO_VALUES);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: outcome cohorts - null or empty");
    }

    @Test
    public void checkNoTargetCohorts() throws IOException {

        String json = getJsonFromFile(JSON_NO_VALUES);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: target cohorts - null or empty");
    }

    @Test
    public void checkNoModelSettings() throws IOException {

        String json = getJsonFromFile(JSON_NO_VALUES);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: model settings - null or empty");
    }

    @Test
    public void checkInvalidModelSettings() throws IOException {

        String json = getJsonFromFile(JSON_INVALID_VALUES);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: model settings - duplicate values");
    }

    @Test
    public void checkNoCovariateSettings() throws IOException {

        String json = getJsonFromFile(JSON_NO_VALUES);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: covariate settings - null or empty");
    }

    @Test
    public void checkInvalidCovariateSettings() throws IOException {

        String json = getJsonFromFile(JSON_INVALID_VALUES);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: covariate settings - duplicate values");
    }

    @Test
    public void checkNoPopulationSettings() throws IOException {

        String json = getJsonFromFile(JSON_NO_VALUES);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: population settings - null or empty");
    }

    @Test
    public void checkInvalidPopulationSettings() throws IOException {

        String json = getJsonFromFile(JSON_INVALID_VALUES);
        PredictionAnalysisDTO dto = Utils.deserialize(json, PredictionAnalysisDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: population settings - duplicate values");
    }
}
