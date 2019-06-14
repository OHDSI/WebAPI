package org.ohdsi.webapi.test.entity.prediction.create;

import org.junit.Test;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.test.entity.prediction.BasePredictionTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPredictionCreate extends BasePredictionTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Arrange
        //reset entity
        firstIncomingEntity = new PredictionAnalysis();
        firstIncomingEntity.setName(NEW_TEST_ENTITY);
        firstIncomingEntity.setSpecification(PR_SPECIFICATION);
        
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
