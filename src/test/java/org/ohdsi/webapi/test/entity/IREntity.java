package org.ohdsi.webapi.test.entity;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.service.IRAnalysisResource;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@RunWith(JUnitParamsRunner.class)
@SpringBootTest(classes = WebApi.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public class IREntity implements TestCreate, TestCopy {
    @Autowired
    protected IRAnalysisResource irAnalysisResource;
    @Autowired
    protected IncidenceRateAnalysisRepository irRepository;
    private IRAnalysisDTO firstSavedDTO;

    // in JUnit 4 it's impossible to mark methods inside interface with annotations, it was implemented in JUnit 5. After upgrade it's needed
    // to mark interface methods with @Test, @Before, @After and to remove them from this class
    @After
    @Override
    public void tearDownDB() {

        irRepository.deleteAll();
    }

    @Before
    @Override
    public void init() throws Exception {

        TestCreate.super.init();
    }

    //region test methods
    @Test
    @Override
    public void shouldNotCreateEntityWithDuplicateName() {

        TestCreate.super.shouldNotCreateEntityWithDuplicateName();
    }

    @Test
    @Override
    public void shouldCopyWithUniqueName() throws Exception {

        TestCopy.super.shouldCopyWithUniqueName();
    }

    @Test
    @Override
    public void shouldCopyFromCopy() throws Exception {

        TestCopy.super.shouldCopyFromCopy();
    }

    @Test
    @Override
    public void shouldCopySeveralTimesOriginal() throws Exception {

        TestCopy.super.shouldCopySeveralTimesOriginal();
    }

    @Test
    @Parameters({
            "abcde, abc, abc", "abcde (1), abcde, abcde (2)"
    })
    @Override
    public void shouldCopyOfPartlySameName(String firstName, String secondName, String assertionName) throws Exception {

        TestCopy.super.shouldCopyOfPartlySameName(firstName, secondName, assertionName);
    }
    //endregion

    @Override
    public Object createCopy(Object dto) {

        return irAnalysisResource.copy(((IRAnalysisDTO) dto).getId());
    }

    @Override
    public String getDtoName(Object dto) {

        return ((IRAnalysisDTO) dto).getName();
    }

    @Override
    public void initFirstDTO() {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    public Object getFirstSavedDTO() {

        return firstSavedDTO;
    }

    @Override
    public IRAnalysisDTO createEntity(String name) {

        IRAnalysisDTO dto = new IRAnalysisDTO();
        dto.setName(name);
        return irAnalysisResource.createAnalysis(dto);
    }

    @Override
    public String getConstraintName() {

        return "uq_ir_name";
    }
}
