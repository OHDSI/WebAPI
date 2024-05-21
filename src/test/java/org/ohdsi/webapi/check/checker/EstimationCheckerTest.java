package org.ohdsi.webapi.check.checker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.checker.characterization.CharacterizationChecker;
import org.ohdsi.webapi.check.checker.estimation.EstimationChecker;
import org.ohdsi.webapi.check.warning.Warning;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

public class EstimationCheckerTest extends BaseCheckerTest {
    private static final String JSON_VALID = "/check/checker/estimation-valid.json";
    private static final String JSON_NO_ANALYSIS_SETTINGS = "/check/checker/estimation-no-analysis-settings.json";
    private static final String JSON_POSITIVE_CONTROL_INVALID_VALUES = "/check/checker/estimation-positive-control-invalid-values.json";
    @Autowired
    private EstimationChecker checker;

    @Test
    public void checkValid() throws IOException {

        String json = getJsonFromFile(JSON_VALID);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertEquals(0, result.getWarnings().size());
    }

    @Test
    public void checkNoSpecification() throws IOException {

        String json = getJsonFromFile(JSON_VALID);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification - null or empty");
    }

    @Test
    public void checkNoAnalysisSettings() throws IOException {

        String json = getJsonFromFile(JSON_NO_ANALYSIS_SETTINGS);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification - null or empty");
    }

    @Test
    public void checkPositiveControlInvalidWindowStart() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: positive control synthesis :: time-at-risk window start - must be greater or equal to 0");
    }

    @Test
    public void checkPositiveControlInvalidWindowEnd() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: positive control synthesis :: time-at-risk window end - must be greater or equal to 0");
    }

    @Test
    public void checkPositiveControlInvalidWashout() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: positive control synthesis :: minimum required continuous observation time - must be greater or equal to 0");
    }

    @Test
    public void checkPositiveControlInvalidMaxSubjects() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: positive control synthesis :: maximum number of people used to fit an outcome model - must be greater or equal to 0");
    }

    @Test
    public void checkPositiveControlInvalidRatioBetweenTargetAndInjected() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: positive control synthesis :: allowed ratio between target and injected signal size - must be greater or equal to 0");
    }

    @Test
    public void checkPositiveControlInvaliOutcomeIdOffset() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: positive control synthesis :: first new outcome ID that is to be created - must be greater or equal to 0");
    }

    @Test
    public void checkPositiveControlInvalidMinOutcomeCountForModel() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: positive control synthesis :: minimum number of outcome events required to build a model - must be greater or equal to 0");
    }

    @Test
    public void checkPositiveControlInvalidMinOutcomeCountForInjection() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: positive control synthesis :: minimum number of outcome events required to inject a signal - must be greater or equal to 0");
    }

    @Test
    public void checkNegativeControlInvalidOccurrenceType() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: negative control outcome :: type of occurrence of the event when selecting from the domain - null or empty");
    }

    @Test
    public void checkNegativeControlInvalidDetectOnDescendants() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: negative control outcome :: using of descendant concepts for the negative control outcome - null or empty");
    }

    @Test
    public void checkNegativeControlInvalidDomains() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: negative control outcome :: domains to detect negative control outcomes - null or empty");
    }

    @Test
    public void checkInvalidNegativeControl() throws IOException {

        String json = getJsonFromFile(JSON_POSITIVE_CONTROL_INVALID_VALUES);
        EstimationDTO dto = Utils.deserialize(json, EstimationDTO.class);
        dto.setSpecification(json);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "specification :: negative control - invalid");
    }
}
