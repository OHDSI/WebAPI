package org.ohdsi.webapi.entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.pathway.PathwayController;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

public class PathwayEntityTest extends AbstractDatabaseTest implements TestCreate, TestCopy<PathwayAnalysisDTO>, TestImport<PathwayAnalysisDTO, PathwayAnalysisExportDTO> {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected PathwayController pwController;
    @Autowired
    protected PathwayAnalysisEntityRepository pwRepository;
    @Autowired
    protected PathwayService pwService;
    private PathwayAnalysisDTO firstSavedDTO;

    // in JUnit 4 it's impossible to mark methods inside interface with annotations, it was implemented in JUnit 5. After upgrade it's needed
    // to mark interface methods with @Test, @Before, @After and to remove them from this class
    @After
    @Override
    public void tearDownDB() {

        pwRepository.deleteAll();
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
    public PathwayAnalysisDTO createCopy(PathwayAnalysisDTO dto) {

        return pwController.copy(dto.getId());
    }

    @Override
    public void initFirstDTO() {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    public PathwayAnalysisDTO getFirstSavedDTO() {

        return firstSavedDTO;
    }

    @Override
    public PathwayAnalysisDTO createEntity(String name) {

        return createEntity(createAndInitIncomingEntity(name));
    }

    @Override
    public PathwayAnalysisDTO createEntity(PathwayAnalysisDTO dto) {

        return pwController.create(dto);
    }

    @Override
    public PathwayAnalysisDTO createAndInitIncomingEntity(String name) {

        PathwayAnalysisDTO dto = new PathwayAnalysisDTO();
        dto.setName(name);
        dto.setEventCohorts(new ArrayList<>());
        dto.setTargetCohorts(new ArrayList<>());
        return dto;
    }

    @Override
    public String getConstraintName() {

        return "uq_pw_name";
    }

    @Override
    public CommonEntity getEntity(int id) {

        return pwService.getById(id);
    }

    @Override
    public PathwayAnalysisExportDTO getExportEntity(CommonEntity entity) {

        return conversionService.convert(entity, PathwayAnalysisExportDTO.class);
    }

    @Override
    public PathwayAnalysisDTO doImport(PathwayAnalysisExportDTO dto) {

        return pwController.importAnalysis(dto);
    }
}
