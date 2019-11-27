package org.ohdsi.webapi.test.entity.conceptset.copy;

import org.junit.Test;

import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.test.entity.conceptset.BaseCSTestEntity;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.webapi.service.ConceptSetService.COPY_NAME;
import static org.ohdsi.webapi.test.entity.TestConstants.COPY_PREFIX;

public class TestCSCopy extends BaseCSTestEntity {

    @Test
    public void testUsualCopy() {       
        
        //Action
        firstSavedDTO.setName(csService.getNameForCopy(firstSavedDTO.getId()).get(COPY_NAME));
        ConceptSetDTO copy = csService.createConceptSet(firstSavedDTO);

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName(), copy.getName());
    }

    @Test
    public void testCopyOfCopy() {
        
        //Action
        firstSavedDTO.setName(csService.getNameForCopy(firstSavedDTO.getId()).get(COPY_NAME));
        ConceptSetDTO firstCopy = csService.createConceptSet(firstSavedDTO);
        firstCopy.setName(csService.getNameForCopy(firstCopy.getId()).get(COPY_NAME));
        ConceptSetDTO secondCopy = csService.createConceptSet(firstCopy);

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + firstIncomingDTO.getName(), secondCopy.getName());
    }

    @Test
    public void testSeveralCopiesOfOriginal() {

        //Action
        firstSavedDTO.setName(csService.getNameForCopy(firstSavedDTO.getId()).get(COPY_NAME));
        ConceptSetDTO firstCopy = csService.createConceptSet(firstSavedDTO);
        firstSavedDTO.setName(csService.getNameForCopy(firstSavedDTO.getId()).get(COPY_NAME));
        ConceptSetDTO secondCopy = csService.createConceptSet(firstSavedDTO);

        //Assert
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName(), firstCopy.getName());
        assertEquals(COPY_PREFIX + firstIncomingDTO.getName() + " (1)", secondCopy.getName());
    }

    @Test
    public void testCopyOfPartlySameName() {

        //Arrange
        ConceptSetDTO secondIncomingDTO = new ConceptSetDTO();
        secondIncomingDTO.setName(COPY_PREFIX + "abcde");
        csService.createConceptSet(secondIncomingDTO);

        ConceptSetDTO thirdIncomingDTO = new ConceptSetDTO();
        thirdIncomingDTO.setName("abc");
        ConceptSetDTO savedDTO = csService.createConceptSet(thirdIncomingDTO);

        //Action
        savedDTO.setName(csService.getNameForCopy(savedDTO.getId()).get(COPY_NAME));
        ConceptSetDTO copy = csService.createConceptSet(savedDTO);

        //Assert
        assertEquals(COPY_PREFIX + "abc", copy.getName());
    }

    @Test
    public void testCopyWhenEntityWithNameExists() {

        //Arrange
        ConceptSetDTO secondIncomingDTO = new ConceptSetDTO();
        secondIncomingDTO.setName(COPY_PREFIX + "abcde (1)");
        csService.createConceptSet(secondIncomingDTO);

        ConceptSetDTO thirdIncomingDTO = new ConceptSetDTO();
        thirdIncomingDTO.setName("abcde");
        ConceptSetDTO savedDTO = csService.createConceptSet(thirdIncomingDTO);

        //Action
        savedDTO.setName(csService.getNameForCopy(savedDTO.getId()).get(COPY_NAME));
        ConceptSetDTO copy = csService.createConceptSet(savedDTO);

        //Assert
        assertEquals(COPY_PREFIX + "abcde (2)", copy.getName());
    }
}
