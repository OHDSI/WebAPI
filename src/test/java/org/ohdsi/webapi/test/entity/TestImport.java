package org.ohdsi.webapi.test.entity;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;
import static org.ohdsi.webapi.test.TestConstants.SOME_UNIQUE_TEST_NAME;

public interface TestImport extends EntityMethods{

    Integer getDtoId(Object dto);

    String getDtoName(Object dto);

    Object getEntity(int id);

    Object getExportEntity(Object entity);

    void setExportName(Object entity, String name);

    Object doImport(Object dto) throws Exception;

    Object createAndInitIncomingEntity(String name);

    Object createEntity(Object dto) throws Exception;

    Object getFirstSavedDTO();

    default void shouldImportUniqueName() throws Exception {

        //Arrange
        Object savedEntity = getEntity(getDtoId(getFirstSavedDTO()));
        Object exportedEntity = getExportEntity(savedEntity);
        setExportName(exportedEntity, SOME_UNIQUE_TEST_NAME);

        //Action
        Object firstImport = doImport(exportedEntity);

        //Assert
        assertEquals(SOME_UNIQUE_TEST_NAME, getDtoName(firstImport));
    }

    default void shouldImportWithTheSameName() throws Exception {

        //Arrange
        Object savedEntity = getEntity(getDtoId(getFirstSavedDTO()));
        Object exportDTO = getExportEntity(savedEntity);

        //Action
        Object firstImport = doImport(exportDTO);
        //reset dto
        exportDTO = getExportEntity(savedEntity);
        Object secondImport = doImport(exportDTO);

        //Assert
        assertEquals(NEW_TEST_ENTITY + " (1)", getDtoName(firstImport));
        assertEquals(NEW_TEST_ENTITY + " (2)", getDtoName(secondImport));
    }

    default void shouldImportWhenEntityWithNameExists() throws Exception {

        //Arrange
        Object firstCreatedEntity = getEntity(getDtoId(getFirstSavedDTO()));
        Object firstExportDTO = getExportEntity(firstCreatedEntity);

        Object secondIncomingEntity = createAndInitIncomingEntity(NEW_TEST_ENTITY + " (1)");
        //save "New test entity (1)" to DB
        createEntity(secondIncomingEntity);

        Object thirdIncomingEntity = createAndInitIncomingEntity(NEW_TEST_ENTITY + " (1) (2)");
        //save "New test entity (1) (2)" to DB
        Object thirdSavedDTO = createEntity(thirdIncomingEntity);
        Object thirdCreatedEntity = getEntity(getDtoId(thirdSavedDTO));
        Object thirdExportDTO = getExportEntity(thirdCreatedEntity);

        //Action
        //import of "New test entity"
        Object firstImport = doImport(firstExportDTO);
        //import of "New test entity (1) (2)"
        Object secondImport = doImport(thirdExportDTO);

        Object fourthIncomingEntity = createAndInitIncomingEntity(NEW_TEST_ENTITY + " (1) (2) (2)");
        //save "New test entity (1) (2) (2)" to DB
        createEntity(fourthIncomingEntity);

        //reset dto
        thirdExportDTO = getExportEntity(thirdCreatedEntity);
        //import of "New test entity (1) (2)"
        Object thirdImport = doImport(thirdExportDTO);

        //Assert
        assertEquals(NEW_TEST_ENTITY + " (2)", getDtoName(firstImport));
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (1)", getDtoName(secondImport));
        assertEquals(NEW_TEST_ENTITY + " (1) (2) (3)", getDtoName(thirdImport));
    }
}
