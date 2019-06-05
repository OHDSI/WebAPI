package org.ohdsi.webapi.test.entity.estimation.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.estimation.BaseEstimationTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestEstimationCreate extends BaseEstimationTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            esController.createEstimation(firstIncomingEntity);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(e.getCause().getCause().getCause().getCause().getMessage().contains("uq_es_name"));
        }
    }
}
