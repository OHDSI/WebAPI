package org.ohdsi.webapi.test.entity.incidencerate.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.incidencerate.BaseIRTestEntity;

import static org.junit.Assert.assertEquals;

public class TestIRCreate extends BaseIRTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            irAnalysisResource.createAnalysis(firstIncomingDTO);
        } catch (Exception e) {

        //Assert
        assertEquals(e.getCause().getCause().getCause().getCause().getMessage(),
                    "ERROR: duplicate key value violates unique constraint \"uq_ir_name\"\n  Detail: Key (name)=(" + NEW_TEST_ENTITY + ") already exists.");
        }
    }
}
