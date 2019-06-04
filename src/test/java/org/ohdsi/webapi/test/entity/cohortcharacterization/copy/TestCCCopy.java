package org.ohdsi.webapi.test.entity.cohortcharacterization.copy;

import org.junit.Test;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.test.entity.cohortcharacterization.BaseCCTestEntity;

import static org.junit.Assert.assertEquals;

public class TestCCCopy extends BaseCCTestEntity {

    @Test
    public void testUsualCopy() {

        //Action
        CohortCharacterizationDTO copy = ccController.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName(), copy.getName());
    }

    @Test
    public void testCopyOfCopy() {

        //Action
        CohortCharacterizationDTO firstCopy = ccController.copy(firstSavedDTO.getId());
        CohortCharacterizationDTO secondCopy = ccController.copy(firstCopy.getId());

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + firstIncomingDTO.getName(), secondCopy.getName());
    }

    @Test
    public void testSeveralCopiesOfOriginal() {

        //Action
        CohortCharacterizationDTO firstCopy = ccController.copy(firstSavedDTO.getId());
        CohortCharacterizationDTO secondCopy = ccController.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName(), firstCopy.getName());
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName() + " (1)", secondCopy.getName());
    }

    @Test
    public void testCopyOfPartlySameName() {
        
        //Arrange
        CohortCharacterizationDTO secondIncomingDTO = new CohortCharacterizationDTO();
        secondIncomingDTO.setName(COPY_PREFIX + "abcde");
        ccController.create(secondIncomingDTO);

        CohortCharacterizationDTO thirdIncomingDTO = new CohortCharacterizationDTO();
        thirdIncomingDTO.setName("abc");
        CohortCharacterizationDTO savedDTO = ccController.create(thirdIncomingDTO);
        
        //Action
        CohortCharacterizationDTO copy = ccController.copy(savedDTO.getId());
        
        //Assert
        assertEquals(COPY_PREFIX + "abc", copy.getName());
    }
    
    @Test
    public void testCopyWhenEntityWithNameExists() {

        //Arrange
        CohortCharacterizationDTO secondIncomingDTO = new CohortCharacterizationDTO();
        secondIncomingDTO.setName(COPY_PREFIX + "abcde (1)");
        ccController.create(secondIncomingDTO);

        CohortCharacterizationDTO thirdIncomingDTO = new CohortCharacterizationDTO();
        thirdIncomingDTO.setName("abcde");
        CohortCharacterizationDTO savedDTO = ccController.create(thirdIncomingDTO);

        //Action
        CohortCharacterizationDTO copy = ccController.copy(savedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + "abcde (2)", copy.getName());
    }
}
