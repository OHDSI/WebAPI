package org.ohdsi.webapi.test.entity.prediction.copy;

import org.junit.Test;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.test.entity.prediction.BasePredictionTestEntity;

import static org.junit.Assert.assertEquals;

public class TestPredictionCopy extends BasePredictionTestEntity {

    @Test
    public void testUsualCopy() {

        //Action
        PredictionAnalysisDTO copy = prController.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingEntity.getName(), copy.getName());
    }

    @Test
    public void testCopyOfCopy() {

        //Action
        PredictionAnalysisDTO firstCopy = prController.copy(firstSavedDTO.getId());
        PredictionAnalysisDTO secondCopy = prController.copy(firstCopy.getId());

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + firstIncomingEntity.getName(), secondCopy.getName());
    }

    @Test
    public void testSeveralCopiesOfOriginal() {

        //Action
        PredictionAnalysisDTO firstCopy = prController.copy(firstSavedDTO.getId());
        PredictionAnalysisDTO secondCopy = prController.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingEntity.getName(), firstCopy.getName());
        assertEquals(COPY_PREFIX + firstIncomingEntity.getName() + " (1)", secondCopy.getName());
    }

    @Test
    public void testCopyOfPartlySameName() {
        
        //Arrange
        PredictionAnalysis secondIncomingEntity = new PredictionAnalysis();
        secondIncomingEntity.setName(COPY_PREFIX + "abcde");
        secondIncomingEntity.setSpecification(PR_SPECIFICATION);
        prController.createAnalysis(secondIncomingEntity);

        PredictionAnalysis thirdIncomingEntity = new PredictionAnalysis();
        thirdIncomingEntity.setName("abc");
        thirdIncomingEntity.setSpecification(PR_SPECIFICATION);
        PredictionAnalysisDTO savedDTO = prController.createAnalysis(thirdIncomingEntity);
        
        //Action
        PredictionAnalysisDTO copy = prController.copy(savedDTO.getId());
        
        //Assert
        assertEquals(COPY_PREFIX + "abc", copy.getName());
    }
    
    @Test
    public void testCopyWhenEntityWithNameExists() {

        //Arrange
        PredictionAnalysis secondIncomingEntity = new PredictionAnalysis();
        secondIncomingEntity.setName(COPY_PREFIX + "abcde (1)");
        secondIncomingEntity.setSpecification(PR_SPECIFICATION);
        prController.createAnalysis(secondIncomingEntity);

        PredictionAnalysis thirdIncomingEntity = new PredictionAnalysis();
        thirdIncomingEntity.setName("abcde");
        thirdIncomingEntity.setSpecification(PR_SPECIFICATION);
        PredictionAnalysisDTO savedDTO = prController.createAnalysis(thirdIncomingEntity);

        //Action
        PredictionAnalysisDTO copy = prController.copy(savedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + "abcde (2)", copy.getName());
    }
}
