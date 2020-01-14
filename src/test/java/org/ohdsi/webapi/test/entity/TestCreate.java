package org.ohdsi.webapi.test.entity;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import junitparams.JUnitParamsRunner;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;

@RunWith(JUnitParamsRunner.class)
@SpringBootTest(classes = WebApi.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public abstract class TestCreate {

    protected abstract void initFirstDTO() throws Exception;

    protected abstract Object createEntity(String name) throws Exception;

    protected abstract String getConstraintName();

    @Before
    public void setupDB() throws Exception {

        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        initFirstDTO();
    }

    @After
    public abstract void tearDownDB();

    @Test
    public void testCreateWithDuplicateName() {
        //Action
        try {
            createEntity(NEW_TEST_ENTITY);
            fail();
        } catch (Exception e) {

            //Assert
            assertTrue(ExceptionUtils.getRootCauseMessage(e).contains(getConstraintName()));
        }
    }
}
