package org.ohdsi.webapi.entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.service.IRAnalysisResource;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

public class IREntityTest extends AbstractDatabaseTest implements TestCreate, TestCopy<IRAnalysisDTO> {
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
    @Override
    public void shouldCopyOfPartlySameName() throws Exception {

        TestCopy.super.shouldCopyOfPartlySameName();
    }

    @Override
    public IRAnalysisDTO createCopy(IRAnalysisDTO dto) {

        return irAnalysisResource.copy(dto.getId());
    }

    @Override
    public void initFirstDTO() {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    public IRAnalysisDTO getFirstSavedDTO() {

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
