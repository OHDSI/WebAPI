package org.ohdsi.webapi.test.entity;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.webapi.test.TestConstants.COPY_PREFIX;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

public interface TestCopy extends EntityMethods{

    Object createCopy(Object dto) throws Exception;

    String getDtoName(Object dto);

    Object getFirstSavedDTO();
    
    default void shouldCopyWithUniqueName() throws Exception {

        //Action
        Object copy = createCopy(getFirstSavedDTO());

        //Assert
        assertEquals(COPY_PREFIX + NEW_TEST_ENTITY, getDtoName(copy));
    }
    
    default void shouldCopyFromCopy() throws Exception {

        //Action
        Object firstCopy = createCopy(getFirstSavedDTO());
        Object secondCopy = createCopy(firstCopy);

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + NEW_TEST_ENTITY, getDtoName(secondCopy));
    }
    
    default void shouldCopySeveralTimesOriginal() throws Exception {

        //Action
        Object firstCopy = createCopy(getFirstSavedDTO());
        Object secondCopy = createCopy(getFirstSavedDTO());

        //Assert
        assertEquals(COPY_PREFIX + NEW_TEST_ENTITY, getDtoName(firstCopy));
        assertEquals(COPY_PREFIX + NEW_TEST_ENTITY + " (1)", getDtoName(secondCopy));
    }
    
    default void shouldCopyOfPartlySameName(String firstName, String secondName, String assertionName) throws Exception {

        //Arrange
        createEntity(COPY_PREFIX + firstName);
        Object savedDTO = createEntity(secondName);

        //Action
        Object copy = createCopy(savedDTO);

        //Assert
        assertEquals(COPY_PREFIX + assertionName, getDtoName(copy));
    }
}
