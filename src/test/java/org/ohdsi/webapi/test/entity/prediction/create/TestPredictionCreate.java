package org.ohdsi.webapi.test.entity.prediction.create;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.test.entity.prediction.BasePredictionTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPredictionCreate extends BasePredictionTestEntity {

    private final static String CONSTRAINT_NAME = "uq_pd_name";

    @Test
    public void testCreateWithDuplicateName() {
        //Arrange
        //reset entity
        firstIncomingEntity = new PredictionAnalysis();
        firstIncomingEntity.setName(NEW_TEST_ENTITY);
        firstIncomingEntity.setSpecification(PLP_SPECIFICATION);
        
        //Action
        try {
            plpController.createAnalysis(firstIncomingEntity);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(ExceptionUtils.getRootCauseMessage(e).contains(CONSTRAINT_NAME));
        }
    }
}
