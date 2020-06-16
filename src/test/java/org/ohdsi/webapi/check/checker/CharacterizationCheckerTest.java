package org.ohdsi.webapi.check.checker;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.checker.characterization.CharacterizationChecker;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class CharacterizationCheckerTest extends BaseCheckerTest {
    private static final String JSON_VALID = "/check/checker/characterization-valid.json";
    private static final String JSON_INVALID = "/check/checker/characterization-invalid.json";

    @Test
    public void checkValid() throws IOException {
        String json = getJsonFromFile(JSON_VALID);
        CohortCharacterizationDTO dto = Utils.deserialize(json, CohortCharacterizationDTO.class);

        Checker<CohortCharacterizationDTO> checker = new CharacterizationChecker();
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertEquals(0, result.getWarnings().size());
    }

    @Test
    public void checkInvalid() throws IOException {
        String json = getJsonFromFile(JSON_INVALID);
        CohortCharacterizationDTO dto = Utils.deserialize(json, CohortCharacterizationDTO.class);

        Checker<CohortCharacterizationDTO> checker = new CharacterizationChecker();
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertNotEquals(0, result.getWarnings().size());
    }
}