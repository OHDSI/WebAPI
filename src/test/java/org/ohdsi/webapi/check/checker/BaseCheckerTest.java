package org.ohdsi.webapi.check.checker;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
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

public abstract class BaseCheckerTest {
    protected String getJsonFromFile(String path) throws IOException {
        File file = ResourceUtils.getFile(this.getClass().getResource(path));
        return FileUtils.readFileToString(file, Charset.defaultCharset());
    }
}