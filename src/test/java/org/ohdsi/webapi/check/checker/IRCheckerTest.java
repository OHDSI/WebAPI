package org.ohdsi.webapi.check.checker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.checker.ir.IRChecker;
import org.ohdsi.webapi.check.warning.Warning;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;

import java.io.IOException;
import java.util.Optional;

public class IRCheckerTest extends BaseCheckerTest {
    private static final String JSON_VALID = "/check/checker/ir-valid.json";
    private static final String JSON_NO_EXPRESSION = "/check/checker/ir-no-expression.json";
    private static final String JSON_NO_COHORTS = "/check/checker/ir-no-cohorts.json";
    private IRChecker checker;

    @Before
    public void setUp() {

        checker = new IRChecker();
        checker.init();
    }

    @Test
    public void checkValid() throws IOException {

        String json = getJsonFromFile(JSON_VALID);
        IRAnalysisDTO dto = Utils.deserialize(json, IRAnalysisDTO.class);


        Checker<IRAnalysisDTO> checker = this.checker;
        CheckResult result = new CheckResult(checker.check(dto));
        Assert.assertEquals(0, result.getWarnings().size());
    }

    @Test
    public void checkEmptyExpression() throws IOException {

        String json = getJsonFromFile(JSON_NO_EXPRESSION);
        IRAnalysisDTO dto = Utils.deserialize(json, IRAnalysisDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "expression - null or empty");
    }

    @Test
    public void checkNoOutcomeCohorts() throws IOException {

        String json = getJsonFromFile(JSON_NO_COHORTS);
        IRAnalysisDTO dto = Utils.deserialize(json, IRAnalysisDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "outcome cohorts - null or empty");
    }

    @Test
    public void checkNoTargetCohorts() throws IOException {

        String json = getJsonFromFile(JSON_NO_COHORTS);
        IRAnalysisDTO dto = Utils.deserialize(json, IRAnalysisDTO.class);

        CheckResult result = new CheckResult(checker.check(dto));
        checkWarning(result, "target cohorts - null or empty");
    }
}
