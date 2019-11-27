package org.ohdsi.webapi.test.entity.cohortdefinition.copy;

import org.junit.Test;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.test.entity.cohortdefinition.BaseCDTestEntity;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.webapi.test.entity.TestConstants.COPY_PREFIX;

public class TestCDCopy extends BaseCDTestEntity {

    @Test
    public void testUsualCopy() {

        //Action
        CohortDTO copy = cdService.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName(), copy.getName());
    }

    @Test
    public void testCopyOfCopy() {

        //Action
        CohortDTO firstCopy = cdService.copy(firstSavedDTO.getId());
        CohortDTO secondCopy = cdService.copy(firstCopy.getId());

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + firstIncomingDTO.getName(), secondCopy.getName());
    }

    @Test
    public void testSeveralCopiesOfOriginal() {

        //Action
        CohortDTO firstCopy = cdService.copy(firstSavedDTO.getId());
        CohortDTO secondCopy = cdService.copy(firstSavedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName(), firstCopy.getName());
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName() + " (1)", secondCopy.getName());
    }

    @Test
    public void testCopyOfPartlySameName() {

        //Arrange
        CohortDTO secondIncomingDTO = new CohortDTO();
        secondIncomingDTO.setName(COPY_PREFIX + "abcde");
        cdService.createCohortDefinition(secondIncomingDTO);

        CohortDTO thirdIncomingDTO = new CohortDTO();
        thirdIncomingDTO.setName("abc");
        CohortDTO savedDTO = cdService.createCohortDefinition(thirdIncomingDTO);

        //Action
        CohortDTO copy = cdService.copy(savedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + "abc", copy.getName());
    }

    @Test
    public void testCopyWhenEntityWithNameExists() {

        //Arrange
        CohortDTO secondIncomingDTO = new CohortDTO();
        secondIncomingDTO.setName(COPY_PREFIX + "abcde (1)");
        cdService.createCohortDefinition(secondIncomingDTO);

        CohortDTO thirdIncomingDTO = new CohortDTO();
        thirdIncomingDTO.setName("abcde");
        CohortDTO savedDTO = cdService.createCohortDefinition(thirdIncomingDTO);

        //Action
        CohortDTO copy = cdService.copy(savedDTO.getId());

        //Assert
        assertEquals(COPY_PREFIX + "abcde (2)", copy.getName());
    }
}
