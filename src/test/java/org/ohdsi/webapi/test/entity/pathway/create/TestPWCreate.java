package org.ohdsi.webapi.test.entity.pathway.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.pathway.BasePWTestEntity;

import static org.junit.Assert.assertEquals;

public class TestPWCreate extends BasePWTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            pwController.create(firstIncomingDTO);
        } catch (Exception e) {

        //Assert
        assertEquals(e.getCause().getCause().getCause().getCause().getMessage(),
                    "ERROR: duplicate key value violates unique constraint \"uq_pw_name\"\n  Detail: Key (name)=(" + NEW_TEST_ENTITY + ") already exists.");
        }
    }
}
