package org.ohdsi.webapi.test.entity.cohortdefinition.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.cohortdefinition.BaseCDTestEntity;

import static org.junit.Assert.assertEquals;

public class TestCDCreate extends BaseCDTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            cdService.createCohortDefinition(firstIncomingDTO);
        } catch (Exception e) {

        //Assert
        assertEquals(e.getCause().getCause().getMessage(),
                    "ERROR: duplicate key value violates unique constraint \"uq_cd_name\"\n  Detail: Key (name)=(" + NEW_TEST_ENTITY + ") already exists.");
        }
    }
}
