package org.ohdsi.webapi.test.entity.prediction.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.prediction.BasePredictionTestEntity;

import static org.junit.Assert.assertEquals;

public class TestPredictionCreate extends BasePredictionTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            prController.createAnalysis(firstIncomingEntity);
        } catch (Exception e) {

        //Assert
        assertEquals(e.getCause().getCause().getCause().getCause().getMessage(),
                    "ERROR: duplicate key value violates unique constraint \"uq_pd_name\"\n  Detail: Key (name)=(" + NEW_TEST_ENTITY + ") already exists.");
        }
    }
}
