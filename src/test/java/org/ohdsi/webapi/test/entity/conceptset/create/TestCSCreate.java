package org.ohdsi.webapi.test.entity.conceptset.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.conceptset.BaseCSTestEntity;

import static org.junit.Assert.assertEquals;

public class TestCSCreate extends BaseCSTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            csService.createConceptSet(firstIncomingDTO);
        } catch (Exception e) {

        //Assert
        assertEquals(e.getCause().getCause().getMessage(),
                    "ERROR: duplicate key value violates unique constraint \"uq_cs_name\"\n  Detail: Key (concept_set_name)=(" + NEW_TEST_ENTITY + ") already exists.");
        }
    }
}
