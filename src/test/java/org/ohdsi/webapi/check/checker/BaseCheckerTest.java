package org.ohdsi.webapi.check.checker;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.checker.characterization.CharacterizationChecker;
import org.ohdsi.webapi.check.warning.Warning;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

public abstract class BaseCheckerTest extends AbstractDatabaseTest {
    protected String getJsonFromFile(String path) throws IOException {
        File file = ResourceUtils.getFile(this.getClass().getResource(path));
        return FileUtils.readFileToString(file, Charset.defaultCharset());
    }

    protected void checkWarning(CheckResult result, String message) {

        List<String> warningMessages = result.getWarnings().stream().map(Warning::toMessage).collect(Collectors.toList());

        Assert.assertThat(warningMessages, Matchers.hasItem(message));
    }
}
