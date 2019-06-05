package org.ohdsi.webapi.test.entity.cohortdefinition.create;

import org.junit.Test;
import org.ohdsi.webapi.test.entity.cohortdefinition.BaseCDTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestCDCreate extends BaseCDTestEntity {

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            cdService.createCohortDefinition(firstIncomingDTO);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(e.getCause().getCause().getMessage().contains("uq_cd_name"));
        }
    }
}
