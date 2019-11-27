package org.ohdsi.webapi.test.entity.estimation.copy;

import org.junit.Test;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.test.entity.estimation.BaseEstimationTestEntity;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.analysis.estimation.design.EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;
import static org.ohdsi.webapi.test.entity.TestConstants.COPY_PREFIX;

public class TestEstimationCopy extends BaseEstimationTestEntity {

    @Test
    public void testUsualCopy() throws Exception {

        //Action
        EstimationDTO copy = pleController.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingEntity.getName(), copy.getName());
    }

    @Test
    public void testCopyOfCopy() throws Exception {

        //Action
        EstimationDTO firstCopy = pleController.copy(firstSavedDTO.getId());
        EstimationDTO secondCopy = pleController.copy(firstCopy.getId());

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + firstIncomingEntity.getName(), secondCopy.getName());
    }

    @Test
    public void testSeveralCopiesOfOriginal() throws Exception {

        //Action
        EstimationDTO firstCopy = pleController.copy(firstSavedDTO.getId());
        EstimationDTO secondCopy = pleController.copy(firstSavedDTO.getId());

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
        secondIncomingEntity.setSpecification(PLE_SPECIFICATION);
        pleController.createEstimation(secondIncomingEntity);

        Estimation thirdIncomingEntity = new Estimation();
        thirdIncomingEntity.setName("abc");
        thirdIncomingEntity.setType(COMPARATIVE_COHORT_ANALYSIS);
        thirdIncomingEntity.setSpecification(PLE_SPECIFICATION);
        EstimationDTO savedDTO = pleController.createEstimation(thirdIncomingEntity);
        
        //Action
        EstimationDTO copy = pleController.copy(savedDTO.getId());
        
        //Assert
        assertEquals(COPY_PREFIX + "abc", copy.getName());
    }
    
    @Test
    public void testCopyWhenEntityWithNameExists() throws Exception {

        //Arrange
        Estimation secondIncomingEntity = new Estimation();
        secondIncomingEntity.setName(COPY_PREFIX + "abcde (1)");
        secondIncomingEntity.setType(COMPARATIVE_COHORT_ANALYSIS);
        secondIncomingEntity.setSpecification(PLE_SPECIFICATION);
        pleController.createEstimation(secondIncomingEntity);

        Estimation thirdIncomingEntity = new Estimation();
        thirdIncomingEntity.setName("abcde");
        thirdIncomingEntity.setType(COMPARATIVE_COHORT_ANALYSIS);
        thirdIncomingEntity.setSpecification(PLE_SPECIFICATION);
        EstimationDTO savedDTO = pleController.createEstimation(thirdIncomingEntity);

        //Action
        EstimationDTO copy = pleController.copy(savedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + "abcde (2)", copy.getName());
    }
}
