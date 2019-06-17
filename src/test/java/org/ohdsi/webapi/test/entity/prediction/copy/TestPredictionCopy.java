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
        PredictionAnalysisDTO copy = plpController.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingEntity.getName(), copy.getName());
    }

    @Test
    public void testCopyOfCopy() {

        //Action
        PredictionAnalysisDTO firstCopy = plpController.copy(firstSavedDTO.getId());
        PredictionAnalysisDTO secondCopy = plpController.copy(firstCopy.getId());

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + firstIncomingEntity.getName(), secondCopy.getName());
    }

    @Test
    public void testSeveralCopiesOfOriginal() {

        //Action
        PredictionAnalysisDTO firstCopy = plpController.copy(firstSavedDTO.getId());
        PredictionAnalysisDTO secondCopy = plpController.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingEntity.getName(), firstCopy.getName());
        assertEquals(COPY_PREFIX + firstIncomingEntity.getName() + " (1)", secondCopy.getName());
    }

    @Test
    public void testCopyOfPartlySameName() {
        
        //Arrange
        PredictionAnalysis secondIncomingEntity = new PredictionAnalysis();
        secondIncomingEntity.setName(COPY_PREFIX + "abcde");
        secondIncomingEntity.setSpecification(PLP_SPECIFICATION);
        plpController.createAnalysis(secondIncomingEntity);

        PredictionAnalysis thirdIncomingEntity = new PredictionAnalysis();
        thirdIncomingEntity.setName("abc");
        thirdIncomingEntity.setSpecification(PLP_SPECIFICATION);
        PredictionAnalysisDTO savedDTO = plpController.createAnalysis(thirdIncomingEntity);
        
        //Action
        PredictionAnalysisDTO copy = plpController.copy(savedDTO.getId());
        
        //Assert
        assertEquals(COPY_PREFIX + "abc", copy.getName());
    }
    
    @Test
    public void testCopyWhenEntityWithNameExists() {

        //Arrange
        PredictionAnalysis secondIncomingEntity = new PredictionAnalysis();
        secondIncomingEntity.setName(COPY_PREFIX + "abcde (1)");
        secondIncomingEntity.setSpecification(PLP_SPECIFICATION);
        plpController.createAnalysis(secondIncomingEntity);

        PredictionAnalysis thirdIncomingEntity = new PredictionAnalysis();
        thirdIncomingEntity.setName("abcde");
        thirdIncomingEntity.setSpecification(PLP_SPECIFICATION);
        PredictionAnalysisDTO savedDTO = plpController.createAnalysis(thirdIncomingEntity);

        //Action
        PredictionAnalysisDTO copy = plpController.copy(savedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + "abcde (2)", copy.getName());
    }
}
