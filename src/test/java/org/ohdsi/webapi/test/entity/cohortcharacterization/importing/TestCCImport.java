package org.ohdsi.webapi.test.entity.cohortcharacterization.importing;

import org.junit.Test;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.test.entity.cohortcharacterization.BaseCCTestEntity;

import static org.junit.Assert.assertEquals;

public class TestCCImport extends BaseCCTestEntity {

    @Test
    public void testImportUniqueName() {

        //Arrange
        CohortCharacterizationEntity savedEntity = ccService.findByIdWithLinkedEntities(firstSavedDTO.getId());
        CcExportDTO exportDTO = conversionService.convert(savedEntity, CcExportDTO.class);
        exportDTO.setName(SOME_UNIQUE_TEST_NAME);

        //Action
        CohortCharacterizationDTO firstImport = ccController.doImport(exportDTO);

        //Assert
        assertEquals(SOME_UNIQUE_TEST_NAME, firstImport.getName());
    }

    @Test
    public void testImportWithTheSameName() {

        //Arrange
        CohortCharacterizationEntity createdEntity = ccService.findByIdWithLinkedEntities(firstSavedDTO.getId());
        CcExportDTO exportDTO = conversionService.convert(createdEntity, CcExportDTO.class);

        //Action
        CohortCharacterizationDTO firstImport = ccController.doImport(exportDTO);
        //reset dto
        exportDTO = conversionService.convert(createdEntity, CcExportDTO.class);
        CohortCharacterizationDTO secondImport = ccController.doImport(exportDTO);

        //Assert
        assertEquals(NEW_TEST_ENTITY + " (1)", firstImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (2)", secondImport.getName());
    }

    @Test
    public void testImportWhenEntityWithNameExists() {
        
        //Arrange
        CohortCharacterizationEntity firstCreatedEntity = ccService.findByIdWithLinkedEntities(firstSavedDTO.getId());
        CcExportDTO firstExportDTO = conversionService.convert(firstCreatedEntity, CcExportDTO.class);
        
        CohortCharacterizationDTO secondIncomingDTO = new CohortCharacterizationDTO();
        secondIncomingDTO.setName(NEW_TEST_ENTITY + " (1)");
        //save "New test entity (1)" to DB
        ccController.create(secondIncomingDTO);

        CohortCharacterizationDTO thirdIncomingDTO = new CohortCharacterizationDTO();
        thirdIncomingDTO.setName(NEW_TEST_ENTITY + " (1) (2)");
        //save "New test entity (1) (2)" to DB
        CohortCharacterizationDTO thirdSavedDTO = ccController.create(thirdIncomingDTO);
        CohortCharacterizationEntity thirdCreatedEntity = ccService.findByIdWithLinkedEntities(thirdSavedDTO.getId());
        CcExportDTO thirdExportDTO = conversionService.convert(thirdCreatedEntity, CcExportDTO.class);
        
        //Action
        //import of "New test entity"
        CohortCharacterizationDTO firstImport = ccController.doImport(firstExportDTO);
        //import of "New test entity (1) (2)"
        CohortCharacterizationDTO secondImport = ccController.doImport(thirdExportDTO);
        
        CohortCharacterizationDTO fourthIncomingDTO = new CohortCharacterizationDTO();
        fourthIncomingDTO.setName(NEW_TEST_ENTITY + " (1) (2) (2)");
        //save "New test entity (1) (2) (2)" to DB
        ccController.create(fourthIncomingDTO);
        
        //reset dto
        thirdExportDTO = conversionService.convert(thirdCreatedEntity, CcExportDTO.class);
        //import of "New test entity (1) (2)"
        CohortCharacterizationDTO thirdImport = ccController.doImport(thirdExportDTO);
        
        //Assert
        assertEquals(NEW_TEST_ENTITY + " (2)", firstImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (1)", secondImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (3)", thirdImport.getName());
    }
}
