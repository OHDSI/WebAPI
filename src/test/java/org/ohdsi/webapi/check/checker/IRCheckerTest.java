package org.ohdsi.webapi.check.checker;

import org.junit.Assert;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.checker.characterization.CharacterizationChecker;
import org.ohdsi.webapi.check.checker.ir.IRChecker;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;

import java.io.IOException;

import static org.junit.Assert.*;

public class IRCheckerTest extends BaseCheckerTest {
    private static final String JSON_VALID = "/check/checker/ir-valid.json";
    private static final String JSON_INVALID = "/check/checker/ir-invalid.json";

    @Test
    public void checkValid() throws IOException {
        String json = getJsonFromFile(JSON_VALID);
        IRAnalysisDTO dto = Utils.deserialize(json, IRAnalysisDTO.class);

        Checker<IRAnalysisDTO> checker = new IRChecker();
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertEquals(0, result.getWarnings().size());
    }

    @Test
    public void checkInvalid() throws IOException {
        String json = getJsonFromFile(JSON_INVALID);
        IRAnalysisDTO dto = Utils.deserialize(json, IRAnalysisDTO.class);

        Checker<IRAnalysisDTO> checker = new IRChecker();
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertNotEquals(0, result.getWarnings().size());
    }
}