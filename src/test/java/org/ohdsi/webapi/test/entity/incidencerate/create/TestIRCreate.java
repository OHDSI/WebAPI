package org.ohdsi.webapi.test.entity.incidencerate.create;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.ohdsi.webapi.test.entity.incidencerate.BaseIRTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestIRCreate extends BaseIRTestEntity {

    private final static String CONSTRAINT_NAME = "uq_ir_name";

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            irAnalysisResource.createAnalysis(firstIncomingDTO);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(ExceptionUtils.getRootCauseMessage(e).contains(CONSTRAINT_NAME));
        }
    }
}
