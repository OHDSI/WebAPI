package org.ohdsi.webapi.entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

public class CohortDefinitionEntityTest extends AbstractDatabaseTest implements TestCreate, TestCopy<CohortDTO> {
    @Autowired
    private CohortDefinitionService cdService;
    @Autowired
    protected CohortDefinitionRepository cdRepository;
    private CohortDTO firstSavedDTO;

    // in JUnit 4 it's impossible to mark methods inside interface with annotations, it was implemented in JUnit 5. After upgrade it's needed
    // to mark interface methods with @Test, @Before, @After and to remove them from this class
    @After
    @Override
    public void tearDownDB() {

        cdRepository.deleteAll();
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
    public CohortDTO createCopy(CohortDTO dto) {

        return cdService.copy(dto.getId());
    }

    @Override
    public void initFirstDTO() {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    public CohortDTO getFirstSavedDTO() {

        return firstSavedDTO;
    }

    @Override
    public String getConstraintName() {

        return "uq_cd_name";
    }

    @Override
    public CohortDTO createEntity(String name) {

        CohortDTO dto = new CohortDTO();
        dto.setName(name);
        return cdService.createCohortDefinition(dto);
    }
}
