package org.ohdsi.webapi.test.entity.cohortcharacterization.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.cohortcharacterization.BaseCCTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestCCCreate extends BaseCCTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            ccController.create(firstIncomingDTO);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(e.getCause().getCause().getCause().getCause().getMessage().contains("uq_cc_name"));
        }
    }
}
