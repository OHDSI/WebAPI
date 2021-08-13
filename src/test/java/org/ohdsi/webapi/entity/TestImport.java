package org.ohdsi.webapi.entity;

import org.ohdsi.webapi.CommonDTO;
import org.ohdsi.webapi.model.CommonEntity;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;
import static org.ohdsi.webapi.test.TestConstants.SOME_UNIQUE_TEST_NAME;

public interface TestImport<T extends CommonDTO, U extends CommonDTO> extends EntityMethods {

    CommonEntity getEntity(int id);

    U getExportEntity(CommonEntity entity);

    T doImport(U dto) throws Exception;

    T createAndInitIncomingEntity(String name);

    T createEntity(T dto) throws Exception;

    T getFirstSavedDTO();

    default void shouldImportUniqueName() throws Exception {

        //Arrange
        CommonEntity savedEntity = getEntity(getFirstSavedDTO().getId().intValue());
        U exportedEntity = getExportEntity(savedEntity);
        exportedEntity.setName(SOME_UNIQUE_TEST_NAME);

        //Action
        T firstImport = doImport(exportedEntity);

        //Assert
        assertEquals(SOME_UNIQUE_TEST_NAME, firstImport.getName());
    }

    default void shouldImportWithTheSameName() throws Exception {

        //Arrange
        CommonEntity savedEntity = getEntity(getFirstSavedDTO().getId().intValue());
        U exportDTO = getExportEntity(savedEntity);

        //Action
        T firstImport = doImport(exportDTO);
        //reset dto
        exportDTO = getExportEntity(savedEntity);
        T secondImport = doImport(exportDTO);

        //Assert
        assertEquals(NEW_TEST_ENTITY + " (1)", firstImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (2)", secondImport.getName());
    }

    default void shouldImportWhenEntityWithNameExists() throws Exception {

        //Arrange
        CommonEntity firstCreatedEntity = getEntity(getFirstSavedDTO().getId().intValue());
        U firstExportDTO = getExportEntity(firstCreatedEntity);

        T secondIncomingEntity = createAndInitIncomingEntity(NEW_TEST_ENTITY + " (1)");
        //save "New test entity (1)" to DB
        createEntity(secondIncomingEntity);

        T thirdIncomingEntity = createAndInitIncomingEntity(NEW_TEST_ENTITY + " (1) (2)");
        //save "New test entity (1) (2)" to DB
        T thirdSavedDTO = createEntity(thirdIncomingEntity);
        CommonEntity thirdCreatedEntity = getEntity(thirdSavedDTO.getId().intValue());
        U thirdExportDTO = getExportEntity(thirdCreatedEntity);

        //Action
        //import of "New test entity"
        T firstImport = doImport(firstExportDTO);
        //import of "New test entity (1) (2)"
        T secondImport = doImport(thirdExportDTO);

        T fourthIncomingEntity = createAndInitIncomingEntity(NEW_TEST_ENTITY + " (1) (2) (2)");
        //save "New test entity (1) (2) (2)" to DB
        createEntity(fourthIncomingEntity);

        //reset dto
        thirdExportDTO = getExportEntity(thirdCreatedEntity);
        //import of "New test entity (1) (2)"
        T thirdImport = doImport(thirdExportDTO);

        //Assert
        assertEquals(NEW_TEST_ENTITY + " (2)", firstImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (1)", secondImport.getName());
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (3)", thirdImport.getName());
    }
}
