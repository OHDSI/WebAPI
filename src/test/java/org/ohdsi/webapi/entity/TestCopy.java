package org.ohdsi.webapi.entity;

import org.ohdsi.webapi.CommonDTO;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.webapi.test.TestConstants.COPY_PREFIX;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

public interface TestCopy<T extends CommonDTO> extends EntityMethods {

    T createCopy(T dto) throws Exception;

    T createEntity(String name) throws Exception;

    T getFirstSavedDTO();

    default void shouldCopyWithUniqueName() throws Exception {

        //Action
        T copy = createCopy(getFirstSavedDTO());

        //Assert
        assertEquals(COPY_PREFIX + NEW_TEST_ENTITY, copy.getName());
    }

    default void shouldCopyFromCopy() throws Exception {

        //Action
        T firstCopy = createCopy(getFirstSavedDTO());
        T secondCopy = createCopy(firstCopy);

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + NEW_TEST_ENTITY, secondCopy.getName());
    }

    default void shouldCopySeveralTimesOriginal() throws Exception {

        //Action
        T firstCopy = createCopy(getFirstSavedDTO());
        T secondCopy = createCopy(getFirstSavedDTO());

        //Assert
        assertEquals(COPY_PREFIX + NEW_TEST_ENTITY, firstCopy.getName());
        assertEquals(COPY_PREFIX + NEW_TEST_ENTITY + " (1)", secondCopy.getName());
    }

    default void shouldCopyOfPartlySameName() throws Exception {
        String firstName = "abcde, abc, abc";
        String secondName = "abcde (1), abcde, abcde (2)";

        //Arrange
        createEntity(COPY_PREFIX + firstName);
        T savedDTO = createEntity(secondName);

        //Action
        T copy = createCopy(savedDTO);

        //Assert
        assertEquals(COPY_PREFIX + secondName, copy.getName());
    }
}
