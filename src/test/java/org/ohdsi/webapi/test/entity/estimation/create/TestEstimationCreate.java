package org.ohdsi.webapi.test.entity.estimation.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.estimation.BaseEstimationTestEntity;

import static org.junit.Assert.assertEquals;

public class TestEstimationCreate extends BaseEstimationTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            esController.createEstimation(firstIncomingEntity);
        } catch (Exception e) {

        //Assert
        assertEquals(e.getCause().getCause().getCause().getCause().getMessage(),
                    "ERROR: duplicate key value violates unique constraint \"uq_es_name\"\n  Detail: Key (name)=(" + NEW_TEST_ENTITY + ") already exists.");
        }
    }
}
