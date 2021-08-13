package org.ohdsi.webapi.entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.beans.factory.annotation.Autowired;

import static org.ohdsi.webapi.service.ConceptSetService.COPY_NAME;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

public class ConceptSetEntityTest extends AbstractDatabaseTest implements TestCreate, TestCopy<ConceptSetDTO> {
    @Autowired
    protected ConceptSetService csService;
    @Autowired
    protected ConceptSetRepository csRepository;
    private ConceptSetDTO firstSavedDTO;

    // in JUnit 4 it's impossible to mark methods inside interface with annotations, it was implemented in JUnit 5. After upgrade it's needed
    // to mark interface methods with @Test, @Before, @After and to remove them from this class
    @After
    @Override
    public void tearDownDB() {

        csRepository.deleteAll();
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
    public ConceptSetDTO createCopy(ConceptSetDTO dto) {

        dto.setName(csService.getNameForCopy(dto.getId()).get(COPY_NAME));
        return csService.createConceptSet(dto);
    }

    @Override
    public void initFirstDTO() {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    public ConceptSetDTO getFirstSavedDTO() {

        return firstSavedDTO;
    }

    @Override
    public ConceptSetDTO createEntity(String name) {

        ConceptSetDTO dto = new ConceptSetDTO();
        dto.setName(name);
        return csService.createConceptSet(dto);
    }

    @Override
    public String getConstraintName() {

        return "uq_cs_name";
    }
}
