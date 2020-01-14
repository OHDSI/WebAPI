package org.ohdsi.webapi.test.entity;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.webapi.test.TestConstants.COPY_PREFIX;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import junitparams.Parameters;
import org.junit.Test;

public abstract class TestCopy extends TestCreate {

    protected abstract Object createCopy(Object dto) throws Exception;

    protected abstract String getDtoName(Object dto);


    protected abstract Object getFirstSavedDTO();

    @Test
    public void testUsualCopy() throws Exception {

        //Action
        Object copy = createCopy(getFirstSavedDTO());

        //Assert
        assertEquals(COPY_PREFIX + NEW_TEST_ENTITY, getDtoName(copy));
    }

    @Test
    public void testCopyOfCopy() throws Exception {

        //Action
        Object firstCopy = createCopy(getFirstSavedDTO());
        Object secondCopy = createCopy(firstCopy);

        //Assert
        assertEquals(COPY_PREFIX + COPY_PREFIX + NEW_TEST_ENTITY, getDtoName(secondCopy));
    }

    @Test
    public void testSeveralCopiesOfOriginal() throws Exception {

        //Action
        Object firstCopy = createCopy(getFirstSavedDTO());
        Object secondCopy = createCopy(getFirstSavedDTO());

        //Assert
        assertEquals(COPY_PREFIX + NEW_TEST_ENTITY, getDtoName(firstCopy));
        assertEquals(COPY_PREFIX + NEW_TEST_ENTITY + " (1)", getDtoName(secondCopy));
    }

    @Test
    @Parameters({
            "abcde, abc, abc", "abcde (1), abcde, abcde (2)"
    })
    public void testCopyOfPartlySameName(String firstName, String secondName, String assertionName) throws Exception {

        //Arrange
        createEntity(COPY_PREFIX + firstName);
        Object savedDTO = createEntity(secondName);

        //Action
        Object copy = createCopy(savedDTO);

        //Assert
        assertEquals(COPY_PREFIX + assertionName, getDtoName(copy));
    }
}
