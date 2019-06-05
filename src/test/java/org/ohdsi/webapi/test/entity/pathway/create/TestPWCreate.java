package org.ohdsi.webapi.test.entity.pathway.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.pathway.BasePWTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPWCreate extends BasePWTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            pwController.create(firstIncomingDTO);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(e.getCause().getCause().getCause().getCause().getMessage().contains("uq_pw_name"));
        }
    }
}
