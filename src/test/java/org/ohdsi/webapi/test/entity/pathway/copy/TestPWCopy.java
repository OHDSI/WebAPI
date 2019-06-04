package org.ohdsi.webapi.test.entity.pathway.copy;

import org.junit.Test;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.test.entity.pathway.BasePWTestEntity;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TestPWCopy extends BasePWTestEntity {

    @Test
    public void testUsualCopy() {

        //Action
        PathwayAnalysisDTO copy = pwController.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName(), copy.getName());
    }

    @Test
    public void testCopyOfCopy() {

        //Action
        PathwayAnalysisDTO firstCopy = pwController.copy(firstSavedDTO.getId());
        PathwayAnalysisDTO secondCopy = pwController.copy(firstCopy.getId());

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + firstIncomingDTO.getName(), secondCopy.getName());
    }

    @Test
    public void testSeveralCopiesOfOriginal() {

        //Action
        PathwayAnalysisDTO firstCopy = pwController.copy(firstSavedDTO.getId());
        PathwayAnalysisDTO secondCopy = pwController.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName(), firstCopy.getName());
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName() + " (1)", secondCopy.getName());
    }

    @Test
    public void testCopyOfPartlySameName() {
        
        //Arrange
        PathwayAnalysisDTO secondIncomingDTO = new PathwayAnalysisDTO();
        secondIncomingDTO.setName(COPY_PREFIX + "abcde");
        secondIncomingDTO.setEventCohorts(new ArrayList<>());
        secondIncomingDTO.setTargetCohorts(new ArrayList<>());
        pwController.create(secondIncomingDTO);

        PathwayAnalysisDTO thirdIncomingDTO = new PathwayAnalysisDTO();
        thirdIncomingDTO.setName("abc");
        thirdIncomingDTO.setEventCohorts(new ArrayList<>());
        thirdIncomingDTO.setTargetCohorts(new ArrayList<>());
        PathwayAnalysisDTO savedDTO = pwController.create(thirdIncomingDTO);
        
        //Action
        PathwayAnalysisDTO copy = pwController.copy(savedDTO.getId());
        
        //Assert
        assertEquals(COPY_PREFIX + "abc", copy.getName());
    }
    
    @Test
    public void testCopyWhenEntityWithNameExists() {

        //Arrange
        PathwayAnalysisDTO secondIncomingDTO = new PathwayAnalysisDTO();
        secondIncomingDTO.setName(COPY_PREFIX + "abcde (1)");
        secondIncomingDTO.setEventCohorts(new ArrayList<>());
        secondIncomingDTO.setTargetCohorts(new ArrayList<>());
        pwController.create(secondIncomingDTO);

        PathwayAnalysisDTO thirdIncomingDTO = new PathwayAnalysisDTO();
        thirdIncomingDTO.setName("abcde");
        thirdIncomingDTO.setEventCohorts(new ArrayList<>());
        thirdIncomingDTO.setTargetCohorts(new ArrayList<>());
        PathwayAnalysisDTO savedDTO = pwController.create(thirdIncomingDTO);

        //Action
        PathwayAnalysisDTO copy = pwController.copy(savedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + "abcde (2)", copy.getName());
    }
}
