package org.ohdsi.webapi.test.entity.cohortcharacterization.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.cohortcharacterization.BaseCCTestEntity;

import static org.junit.Assert.assertEquals;

public class TestCCCreate extends BaseCCTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            ccController.create(firstIncomingDTO);
        } catch (Exception e) {

        //Assert
        assertEquals(e.getCause().getCause().getCause().getCause().getMessage(),
                    "ERROR: duplicate key value violates unique constraint \"uq_cc_name\"\n  Detail: Key (name)=(" + NEW_TEST_ENTITY + ") already exists.");
        }
    }
}
