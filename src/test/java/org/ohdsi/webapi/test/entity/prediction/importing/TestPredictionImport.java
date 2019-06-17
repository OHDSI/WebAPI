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
        PredictionAnalysis savedEntity = plpService.getAnalysis(firstSavedDTO.getId());
        PatientLevelPredictionAnalysisImpl exportEntity = plpController.exportAnalysis(savedEntity.getId());
        exportEntity.setName(SOME_UNIQUE_TEST_NAME);

        //Action
        PredictionAnalysisDTO firstImport = plpController.importAnalysis(exportEntity);

        //Assert
        assertEquals(SOME_UNIQUE_TEST_NAME, firstImport.getName());
    }

    @Test
    public void testImportWithTheSameName() throws Exception {

        //Arrange
        PredictionAnalysis createdEntity = plpService.getAnalysis(firstSavedDTO.getId());
        PatientLevelPredictionAnalysisImpl exportEntity = plpController.exportAnalysis(createdEntity.getId());

        //Action
        PredictionAnalysisDTO firstImport = plpController.importAnalysis(exportEntity);
        //reset dto
        exportEntity = plpController.exportAnalysis(createdEntity.getId());
        PredictionAnalysisDTO secondImport = plpController.importAnalysis(exportEntity);

        //Assert
        assertEquals(NEW_TEST_ENTITY + " (1)", firstImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (2)", secondImport.getName());
    }

    @Test
    public void testImportWhenEntityWithNameExists() throws Exception {
        
        //Arrange
        PredictionAnalysis firstCreatedEntity = plpService.getAnalysis(firstSavedDTO.getId());
        PatientLevelPredictionAnalysisImpl firstExportEntity = plpController.exportAnalysis(firstCreatedEntity.getId());

        PredictionAnalysis secondIncomingEntity = new PredictionAnalysis();
        secondIncomingEntity.setName(NEW_TEST_ENTITY + " (1)");
        secondIncomingEntity.setSpecification(PLP_SPECIFICATION);
        //save "New test entity (1)" to DB
        plpController.createAnalysis(secondIncomingEntity);

        PredictionAnalysis thirdIncomingEntity = new PredictionAnalysis();
        thirdIncomingEntity.setName(NEW_TEST_ENTITY + " (1) (2)");
        thirdIncomingEntity.setSpecification(PLP_SPECIFICATION);
        //save "New test entity (1) (2)" to DB
        PredictionAnalysisDTO thirdSavedDTO = plpController.createAnalysis(thirdIncomingEntity);
        PredictionAnalysis thirdCreatedEntity = plpService.getAnalysis(thirdSavedDTO.getId());
        PatientLevelPredictionAnalysisImpl thirdExportEntity = plpController.exportAnalysis(thirdCreatedEntity.getId());
        
        //Action
        //import of "New test entity"
        PredictionAnalysisDTO firstImport = plpController.importAnalysis(firstExportEntity);
        //import of "New test entity (1) (2)"
        PredictionAnalysisDTO secondImport = plpController.importAnalysis(thirdExportEntity);

        PredictionAnalysis fourthIncomingEntity = new PredictionAnalysis();
        fourthIncomingEntity.setName(NEW_TEST_ENTITY + " (1) (2) (2)");
        fourthIncomingEntity.setSpecification(PLP_SPECIFICATION);
        //save "New test entity (1) (2) (2)" to DB
        plpController.createAnalysis(fourthIncomingEntity);
        
        //reset dto
        thirdExportEntity = plpController.exportAnalysis(thirdCreatedEntity.getId());
        //import of "New test entity (1) (2)"
        PredictionAnalysisDTO thirdImport = plpController.importAnalysis(thirdExportEntity);
        
        //Assert
        assertEquals(NEW_TEST_ENTITY + " (2)", firstImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (1)", secondImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (3)", thirdImport.getName());
    }
}
