package org.ohdsi.webapi.test.entity.incidencerate.copy;

import org.junit.Test;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.ohdsi.webapi.test.entity.incidencerate.BaseIRTestEntity;

import static org.junit.Assert.assertEquals;

public class TestIRCopy extends BaseIRTestEntity {

    @Test
    public void testUsualCopy() {

        //Action
        IRAnalysisDTO copy = irAnalysisResource.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName(), copy.getName());
    }

    @Test
    public void testCopyOfCopy() {

        //Action
        IRAnalysisDTO firstCopy = irAnalysisResource.copy(firstSavedDTO.getId());
        IRAnalysisDTO secondCopy = irAnalysisResource.copy(firstCopy.getId());

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + firstIncomingDTO.getName(), secondCopy.getName());
    }

    @Test
    public void testSeveralCopiesOfOriginal() {

        //Action
        IRAnalysisDTO firstCopy = irAnalysisResource.copy(firstSavedDTO.getId());
        IRAnalysisDTO secondCopy = irAnalysisResource.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName(), firstCopy.getName());
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName() + " (1)", secondCopy.getName());
    }

    @Test
    public void testCopyOfPartlySameName() {
        
        //Arrange
        IRAnalysisDTO secondIncomingDTO = new IRAnalysisDTO();
        secondIncomingDTO.setName(COPY_PREFIX + "abcde");
        irAnalysisResource.createAnalysis(secondIncomingDTO);

        IRAnalysisDTO thirdIncomingDTO = new IRAnalysisDTO();
        thirdIncomingDTO.setName("abc");
        IRAnalysisDTO savedDTO = irAnalysisResource.createAnalysis(thirdIncomingDTO);
        
        //Action
        IRAnalysisDTO copy = irAnalysisResource.copy(savedDTO.getId());
        
        //Assert
        assertEquals(COPY_PREFIX + "abc", copy.getName());
    }
    
    @Test
    public void testCopyWhenEntityWithNameExists() {

        //Arrange
        IRAnalysisDTO secondIncomingDTO = new IRAnalysisDTO();
        secondIncomingDTO.setName(COPY_PREFIX + "abcde (1)");
        irAnalysisResource.createAnalysis(secondIncomingDTO);

        IRAnalysisDTO thirdIncomingDTO = new IRAnalysisDTO();
        thirdIncomingDTO.setName("abcde");
        IRAnalysisDTO savedDTO = irAnalysisResource.createAnalysis(thirdIncomingDTO);

        //Action
        IRAnalysisDTO copy = irAnalysisResource.copy(savedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + "abcde (2)", copy.getName());
    }
}
