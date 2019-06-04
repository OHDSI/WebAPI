package org.ohdsi.webapi.test.entity.prediction.importing;

import org.junit.Test;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;
import org.ohdsi.webapi.test.entity.prediction.BasePredictionTestEntity;

import static org.junit.Assert.assertEquals;

public class TestPredictionImport extends BasePredictionTestEntity {

    @Test
    public void testImportUniqueName() throws Exception {

        //Arrange
        PredictionAnalysis savedEntity = prService.getAnalysis(firstSavedDTO.getId());
        PatientLevelPredictionAnalysisImpl exportEntity = prController.exportAnalysis(savedEntity.getId());
        exportEntity.setName(SOME_UNIQUE_TEST_NAME);

        //Action
        PredictionAnalysisDTO firstImport = prController.importAnalysis(exportEntity);

        //Assert
        assertEquals(SOME_UNIQUE_TEST_NAME, firstImport.getName());
    }

    @Test
    public void testImportWithTheSameName() throws Exception {

        //Arrange
        PredictionAnalysis createdEntity = prService.getAnalysis(firstSavedDTO.getId());
        PatientLevelPredictionAnalysisImpl exportEntity = prController.exportAnalysis(createdEntity.getId());

        //Action
        PredictionAnalysisDTO firstImport = prController.importAnalysis(exportEntity);
        //reset dto
        exportEntity = prController.exportAnalysis(createdEntity.getId());
        PredictionAnalysisDTO secondImport = prController.importAnalysis(exportEntity);

        //Assert
        assertEquals(NEW_TEST_ENTITY + " (1)", firstImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (2)", secondImport.getName());
    }

    @Test
    public void testImportWhenEntityWithNameExists() throws Exception {
        
        //Arrange
        PredictionAnalysis firstCreatedEntity = prService.getAnalysis(firstSavedDTO.getId());
        PatientLevelPredictionAnalysisImpl firstExportEntity = prController.exportAnalysis(firstCreatedEntity.getId());

        PredictionAnalysis secondIncomingEntity = new PredictionAnalysis();
        secondIncomingEntity.setName(NEW_TEST_ENTITY + " (1)");
        secondIncomingEntity.setSpecification(PR_SPECIFICATION);
        //save "New test entity (1)" to DB
        prController.createAnalysis(secondIncomingEntity);

        PredictionAnalysis thirdIncomingEntity = new PredictionAnalysis();
        thirdIncomingEntity.setName(NEW_TEST_ENTITY + " (1) (2)");
        thirdIncomingEntity.setSpecification(PR_SPECIFICATION);
        //save "New test entity (1) (2)" to DB
        PredictionAnalysisDTO thirdSavedDTO = prController.createAnalysis(thirdIncomingEntity);
        PredictionAnalysis thirdCreatedEntity = prService.getAnalysis(thirdSavedDTO.getId());
        PatientLevelPredictionAnalysisImpl thirdExportEntity = prController.exportAnalysis(thirdCreatedEntity.getId());
        
        //Action
        //import of "New test entity"
        PredictionAnalysisDTO firstImport = prController.importAnalysis(firstExportEntity);
        //import of "New test entity (1) (2)"
        PredictionAnalysisDTO secondImport = prController.importAnalysis(thirdExportEntity);

        PredictionAnalysis fourthIncomingEntity = new PredictionAnalysis();
        fourthIncomingEntity.setName(NEW_TEST_ENTITY + " (1) (2) (2)");
        fourthIncomingEntity.setSpecification(PR_SPECIFICATION);
        //save "New test entity (1) (2) (2)" to DB
        prController.createAnalysis(fourthIncomingEntity);
        
        //reset dto
        thirdExportEntity = prController.exportAnalysis(thirdCreatedEntity.getId());
        //import of "New test entity (1) (2)"
        PredictionAnalysisDTO thirdImport = prController.importAnalysis(thirdExportEntity);
        
        //Assert
        assertEquals(NEW_TEST_ENTITY + " (2)", firstImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (1)", secondImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (3)", thirdImport.getName());
    }
}
