package org.ohdsi.webapi.check.checker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.checker.pathway.PathwayChecker;
import org.ohdsi.webapi.check.checker.tag.helper.TagHelper;
import org.ohdsi.webapi.check.warning.Warning;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Optional;


public class PathwayCheckerTest extends BaseCheckerTest {
    private static final String JSON_VALID = "/check/checker/pathway-valid.json";
    private static final String JSON_INVALID = "/check/checker/pathway-invalid.json";
    @Autowired
    private PathwayChecker checker;

    @Test
    public void checkValid() throws IOException {

        String json = getJsonFromFile(JSON_VALID);
        PathwayAnalysisDTO dto = Utils.deserialize(json, PathwayAnalysisDTO.class);


        Checker<PathwayAnalysisDTO> checker = this.checker;
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertEquals(0, result.getWarnings().size());
    }

    @Test
    public void checkNoTargetCohorts() throws IOException {

        String json = getJsonFromFile(JSON_INVALID);
        PathwayAnalysisDTO dto = Utils.deserialize(json, PathwayAnalysisDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "target cohorts - null or empty");
    }

    @Test
    public void checkNoEventCohorts() throws IOException {

        String json = getJsonFromFile(JSON_INVALID);
        PathwayAnalysisDTO dto = Utils.deserialize(json, PathwayAnalysisDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "event cohorts - null or empty");
    }

    @Test
    public void checkInvalidMaxPathLength() throws IOException {

        String json = getJsonFromFile(JSON_INVALID);
        PathwayAnalysisDTO dto = Utils.deserialize(json, PathwayAnalysisDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "maximum path length - must be between 1 and 10");
    }

    @Test
    public void checkInvalidMinCellCount() throws IOException {

        String json = getJsonFromFile(JSON_INVALID);
        PathwayAnalysisDTO dto = Utils.deserialize(json, PathwayAnalysisDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "minimum cell count - must be greater or equal to 0");
    }

    @Test
    public void checkInvalidCombinationWindow() throws IOException {

        String json = getJsonFromFile(JSON_INVALID);
        PathwayAnalysisDTO dto = Utils.deserialize(json, PathwayAnalysisDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "combination window - must be greater or equal to 0");
    }
}
