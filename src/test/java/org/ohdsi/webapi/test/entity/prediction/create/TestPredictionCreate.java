package org.ohdsi.webapi.test.entity.prediction.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.prediction.BasePredictionTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPredictionCreate extends BasePredictionTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            prController.createAnalysis(firstIncomingEntity);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(e.getCause().getCause().getCause().getCause().getMessage().contains("uq_pd_name"));
        }
    }
}
