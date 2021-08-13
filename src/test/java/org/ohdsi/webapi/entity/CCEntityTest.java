package org.ohdsi.webapi.entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.ohdsi.webapi.model.CommonEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.transaction.annotation.Transactional;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

public class CCEntityTest extends AbstractDatabaseTest implements TestCreate, TestCopy<CohortCharacterizationDTO>, TestImport<CohortCharacterizationDTO, CcExportDTO> {
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

    @Test
    @Override
    @Transactional
    public void shouldImportUniqueName() throws Exception {

        TestImport.super.shouldImportUniqueName();
    }

    @Test
    @Override
    @Transactional
    public void shouldImportWithTheSameName() throws Exception {

        TestImport.super.shouldImportWithTheSameName();
    }

    @Test
    @Override
    @Transactional
    public void shouldImportWhenEntityWithNameExists() throws Exception {

        TestImport.super.shouldImportWhenEntityWithNameExists();
    }

    @Override
    public CohortCharacterizationDTO createCopy(CohortCharacterizationDTO dto) {

        return ccController.copy(dto.getId());
    }

    @Override
    public void initFirstDTO() {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    public CohortCharacterizationDTO getFirstSavedDTO() {

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
    public CohortCharacterizationDTO createEntity(CohortCharacterizationDTO dto) {

        return ccController.create(dto);
    }

    @Override
    public CohortCharacterizationDTO createAndInitIncomingEntity(String name) {

        CohortCharacterizationDTO dto = new CohortCharacterizationDTO();
        dto.setName(name);
        return dto;
    }

    @Override
    public CohortCharacterizationEntity getEntity(int id) {

        return ccService.findByIdWithLinkedEntities((long) id);
    }

    @Override
    public CcExportDTO getExportEntity(CommonEntity entity) {

        return conversionService.convert(entity, CcExportDTO.class);
    }

    @Override
    public CohortCharacterizationDTO doImport(CcExportDTO dto) {

        return ccController.doImport(dto);
    }
}
