package org.ohdsi.webapi.test.entity.cohortdefinition.create;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.ohdsi.webapi.test.entity.cohortdefinition.BaseCDTestEntity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestCDCreate extends BaseCDTestEntity {

    private final static String CONSTRAINT_NAME = "uq_cd_name";

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            cdService.createCohortDefinition(firstIncomingDTO);
            fail();
        } catch (Exception e) {

        //Assert
            assertTrue(ExceptionUtils.getRootCauseMessage(e).contains(CONSTRAINT_NAME));
        }
    }
}
