package org.ohdsi.webapi.test.entity.conceptset.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.conceptset.BaseCSTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestCSCreate extends BaseCSTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            csService.createConceptSet(firstIncomingDTO);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(e.getCause().getCause().getMessage().contains("uq_cs_name"));
        }
    }
}
