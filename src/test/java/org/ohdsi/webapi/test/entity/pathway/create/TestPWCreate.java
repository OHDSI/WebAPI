package org.ohdsi.webapi.test.entity.pathway.create;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.ohdsi.webapi.test.entity.pathway.BasePWTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPWCreate extends BasePWTestEntity {

    private final static String CONSTRAINT_NAME = "uq_pw_name";

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            pwController.create(firstIncomingDTO);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(ExceptionUtils.getRootCauseMessage(e).contains(CONSTRAINT_NAME));
        }
    }
}
