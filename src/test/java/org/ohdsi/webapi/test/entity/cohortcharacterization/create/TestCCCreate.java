package org.ohdsi.webapi.test.entity.cohortcharacterization.create;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.ohdsi.webapi.test.entity.cohortcharacterization.BaseCCTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestCCCreate extends BaseCCTestEntity {
    
    private final static String CONSTRAINT_NAME = "uq_cc_name";

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            ccController.create(firstIncomingDTO);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(ExceptionUtils.getRootCauseMessage(e).contains(CONSTRAINT_NAME));
        }
    }
}
