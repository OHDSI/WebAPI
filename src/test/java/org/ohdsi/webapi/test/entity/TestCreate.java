package org.ohdsi.webapi.test.entity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import org.apache.commons.lang3.exception.ExceptionUtils;

public interface TestCreate extends EntityMethods{

    String getConstraintName();

    default void shouldNotCreateEntityWithDuplicateName() {
        //Action
        try {
            createEntity(NEW_TEST_ENTITY);
            fail();
        } catch (Exception e) {

            //Assert
            assertTrue(ExceptionUtils.getRootCauseMessage(e).contains(getConstraintName()));
        }
    }
}
