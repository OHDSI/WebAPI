package org.ohdsi.webapi.test.entity.incidencerate.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.incidencerate.BaseIRTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestIRCreate extends BaseIRTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            irAnalysisResource.createAnalysis(firstIncomingDTO);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(e.getCause().getCause().getCause().getCause().getMessage().contains("uq_ir_name"));
        }
    }
}
