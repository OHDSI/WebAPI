package org.ohdsi.webapi.test.entity.conceptset.create;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.ohdsi.webapi.test.entity.conceptset.BaseCSTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestCSCreate extends BaseCSTestEntity {

    private final static String CONSTRAINT_NAME = "uq_cs_name";

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            csService.createConceptSet(firstIncomingDTO);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(ExceptionUtils.getRootCauseMessage(e).contains(CONSTRAINT_NAME));
        }
    }
}
