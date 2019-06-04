package org.ohdsi.webapi.test.entity.estimation.copy;

import org.junit.Test;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.test.entity.estimation.BaseEstimationTestEntity;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.analysis.estimation.design.EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;

public class TestEstimationCopy extends BaseEstimationTestEntity {

    @Test
    public void testUsualCopy() throws Exception {

        //Action
        EstimationDTO copy = esController.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingEntity.getName(), copy.getName());
    }

    @Test
    public void testCopyOfCopy() throws Exception {

        //Action
        EstimationDTO firstCopy = esController.copy(firstSavedDTO.getId());
        EstimationDTO secondCopy = esController.copy(firstCopy.getId());

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + firstIncomingEntity.getName(), secondCopy.getName());
    }

    @Test
    public void testSeveralCopiesOfOriginal() throws Exception {

        //Action
        EstimationDTO firstCopy = esController.copy(firstSavedDTO.getId());
        EstimationDTO secondCopy = esController.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingEntity.getName(), firstCopy.getName());
        assertEquals(COPY_PREFIX + firstIncomingEntity.getName() + " (1)", secondCopy.getName());
    }

    @Test
    public void testCopyOfPartlySameName() throws Exception {
        
        //Arrange
        Estimation secondIncomingEntity = new Estimation();
        secondIncomingEntity.setName(COPY_PREFIX + "abcde");
        secondIncomingEntity.setType(COMPARATIVE_COHORT_ANALYSIS);
        secondIncomingEntity.setSpecification(ES_SPECIFICATION);
        esController.createEstimation(secondIncomingEntity);

        Estimation thirdIncomingEntity = new Estimation();
        thirdIncomingEntity.setName("abc");
        thirdIncomingEntity.setType(COMPARATIVE_COHORT_ANALYSIS);
        thirdIncomingEntity.setSpecification(ES_SPECIFICATION);
        EstimationDTO savedDTO = esController.createEstimation(thirdIncomingEntity);
        
        //Action
        EstimationDTO copy = esController.copy(savedDTO.getId());
        
        //Assert
        assertEquals(COPY_PREFIX + "abc", copy.getName());
    }
    
    @Test
    public void testCopyWhenEntityWithNameExists() throws Exception {

        //Arrange
        Estimation secondIncomingEntity = new Estimation();
        secondIncomingEntity.setName(COPY_PREFIX + "abcde (1)");
        secondIncomingEntity.setType(COMPARATIVE_COHORT_ANALYSIS);
        secondIncomingEntity.setSpecification(ES_SPECIFICATION);
        esController.createEstimation(secondIncomingEntity);

        Estimation thirdIncomingEntity = new Estimation();
        thirdIncomingEntity.setName("abcde");
        thirdIncomingEntity.setType(COMPARATIVE_COHORT_ANALYSIS);
        thirdIncomingEntity.setSpecification(ES_SPECIFICATION);
        EstimationDTO savedDTO = esController.createEstimation(thirdIncomingEntity);

        //Action
        EstimationDTO copy = esController.copy(savedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + "abcde (2)", copy.getName());
    }
}
