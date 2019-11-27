package org.ohdsi.webapi.test.entity.pathway.importing;

import org.junit.Test;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.test.entity.pathway.BasePWTestEntity;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.webapi.test.entity.TestConstants.NEW_TEST_ENTITY;
import static org.ohdsi.webapi.test.entity.TestConstants.SOME_UNIQUE_TEST_NAME;

public class TestPWImport extends BasePWTestEntity {

    @Test
    public void testImportUniqueName() {

        //Arrange
        PathwayAnalysisEntity savedEntity = pwService.getById(firstSavedDTO.getId());        
        PathwayAnalysisExportDTO exportDTO = conversionService.convert(savedEntity, PathwayAnalysisExportDTO.class);
        exportDTO.setName(SOME_UNIQUE_TEST_NAME);

        //Action
        PathwayAnalysisDTO firstImport = pwController.importAnalysis(exportDTO);

        //Assert
        assertEquals(SOME_UNIQUE_TEST_NAME, firstImport.getName());
    }

    @Test
    public void testImportWithTheSameName() {

        //Arrange
        PathwayAnalysisEntity createdEntity = pwService.getById(firstSavedDTO.getId());
        PathwayAnalysisExportDTO exportDTO = conversionService.convert(createdEntity, PathwayAnalysisExportDTO.class);

        //Action
        PathwayAnalysisDTO firstImport = pwController.importAnalysis(exportDTO);
        //reset dto
        exportDTO = conversionService.convert(createdEntity, PathwayAnalysisExportDTO.class);
        PathwayAnalysisDTO secondImport = pwController.importAnalysis(exportDTO);

        //Assert
        assertEquals(NEW_TEST_ENTITY + " (1)", firstImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (2)", secondImport.getName());
    }

    @Test
    public void testImportWhenEntityWithNameExists() {
        
        //Arrange
        PathwayAnalysisEntity firstCreatedEntity = pwService.getById(firstSavedDTO.getId());
        PathwayAnalysisExportDTO firstExportDTO = conversionService.convert(firstCreatedEntity, PathwayAnalysisExportDTO.class);
        
        PathwayAnalysisDTO secondIncomingDTO = new PathwayAnalysisDTO();
        secondIncomingDTO.setName(NEW_TEST_ENTITY + " (1)");
        secondIncomingDTO.setEventCohorts(new ArrayList<>());
        secondIncomingDTO.setTargetCohorts(new ArrayList<>());
        //save "New test entity (1)" to DB
        pwController.create(secondIncomingDTO);

        PathwayAnalysisDTO thirdIncomingDTO = new PathwayAnalysisDTO();
        thirdIncomingDTO.setName(NEW_TEST_ENTITY + " (1) (2)");
        thirdIncomingDTO.setEventCohorts(new ArrayList<>());
        thirdIncomingDTO.setTargetCohorts(new ArrayList<>());
        //save "New test entity (1) (2)" to DB
        PathwayAnalysisDTO thirdSavedDTO = pwController.create(thirdIncomingDTO);
        PathwayAnalysisEntity thirdCreatedEntity = pwService.getById(thirdSavedDTO.getId());
        PathwayAnalysisExportDTO thirdExportDTO = conversionService.convert(thirdCreatedEntity, PathwayAnalysisExportDTO.class);
        
        //Action
        //import of "New test entity"
        PathwayAnalysisDTO firstImport = pwController.importAnalysis(firstExportDTO);
        //import of "New test entity (1) (2)"
        PathwayAnalysisDTO secondImport = pwController.importAnalysis(thirdExportDTO);
        
        PathwayAnalysisDTO fourthIncomingDTO = new PathwayAnalysisDTO();
        fourthIncomingDTO.setName(NEW_TEST_ENTITY + " (1) (2) (2)");
        fourthIncomingDTO.setEventCohorts(new ArrayList<>());
        fourthIncomingDTO.setTargetCohorts(new ArrayList<>());
        //save "New test entity (1) (2) (2)" to DB
        pwController.create(fourthIncomingDTO);
        
        //reset dto
        thirdExportDTO = conversionService.convert(thirdCreatedEntity, PathwayAnalysisExportDTO.class);
        //import of "New test entity (1) (2)"
        PathwayAnalysisDTO thirdImport = pwController.importAnalysis(thirdExportDTO);
        
        //Assert
        assertEquals(NEW_TEST_ENTITY + " (2)", firstImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (1)", secondImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (3)", thirdImport.getName());
    }
}
