package org.ohdsi.webapi.test.entity;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.TestPropertySource;

@RunWith(JUnitParamsRunner.class)
@SpringBootTest(classes = WebApi.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public class CCEntity implements TestCreate, TestCopy, TestImport {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected CcController ccController;
    @Autowired
    protected CcRepository ccRepository;
    @Autowired
    protected CcService ccService;
    private CohortCharacterizationDTO firstSavedDTO;

    // in JUnit 4 it's impossible to mark methods inside interface with annotations, it was implemented in JUnit 5. After upgrade it's needed
    // to mark interface methods with @Test, @Before, @After and to remove them from this class
    @Before
    @Override
    public void init() throws Exception {

        TestCreate.super.init();
    }

    @After
    @Override
    public void tearDownDB() {

        ccRepository.deleteAll();
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

    @Test
    @Override
    public void shouldImportUniqueName() throws Exception {

        TestImport.super.shouldImportUniqueName();
    }

    @Test
    @Override
    public void shouldImportWithTheSameName() throws Exception {

        TestImport.super.shouldImportWithTheSameName();
    }

    @Test
    @Override
    public void shouldImportWhenEntityWithNameExists() throws Exception {

        TestImport.super.shouldImportWhenEntityWithNameExists();
    }
    //endregion

    @Override
    public Object createCopy(Object dto) {

        return ccController.copy(((CohortCharacterizationDTO) dto).getId());
    }

    @Override
    public String getDtoName(Object dto) {

        return ((CohortCharacterizationDTO) dto).getName();
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
    public String getConstraintName() {

        return "uq_cc_name";
    }

    @Override
    public CohortCharacterizationDTO createEntity(String name) {

        return createEntity(createAndInitIncomingEntity(name));
    }

    @Override
    public CohortCharacterizationDTO createEntity(Object dto) {

        return ccController.create((CohortCharacterizationDTO) dto);
    }

    @Override
    public CohortCharacterizationDTO createAndInitIncomingEntity(String name) {

        CohortCharacterizationDTO dto = new CohortCharacterizationDTO();
        dto.setName(name);
        return dto;
    }

    @Override
    public Integer getDtoId(Object dto) {

        return ((CohortCharacterizationDTO) dto).getId().intValue();
    }

    @Override
    public Object getEntity(int id) {

        return ccService.findByIdWithLinkedEntities((long) id);
    }

    @Override
    public Object getExportEntity(Object entity) {

        return conversionService.convert(entity, CcExportDTO.class);
    }

    @Override
    public void setExportName(Object entity, String name) {

        ((CcExportDTO) entity).setName(name);
    }

    @Override
    public Object doImport(Object dto) {

        return ccController.doImport((CcExportDTO) dto);
    }
}
