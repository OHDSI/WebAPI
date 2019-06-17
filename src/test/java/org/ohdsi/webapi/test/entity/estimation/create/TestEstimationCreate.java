package org.ohdsi.webapi.test.entity.estimation.create;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.test.entity.estimation.BaseEstimationTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ohdsi.analysis.estimation.design.EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;

public class TestEstimationCreate extends BaseEstimationTestEntity {

    private final static String CONSTRAINT_NAME = "uq_es_name";

    @Test
    public void testCreateWithDuplicateName() {
        //Arrange
        //reset entity
        firstIncomingEntity = new Estimation();
        firstIncomingEntity.setName(NEW_TEST_ENTITY);
        firstIncomingEntity.setType(COMPARATIVE_COHORT_ANALYSIS);
        firstIncomingEntity.setSpecification(PLE_SPECIFICATION);
        
        //Action
        try {
            pleController.createEstimation(firstIncomingEntity);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(ExceptionUtils.getRootCauseMessage(e).contains(CONSTRAINT_NAME));
        }
    }
}
