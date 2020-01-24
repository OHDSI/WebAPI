package org.ohdsi.webapi.test.entity;

import org.springframework.test.context.TestContextManager;

public interface EntityMethods {

    void initFirstDTO() throws Exception;

    Object createEntity(String name) throws Exception;

    default void init() throws Exception {

        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        initFirstDTO();
    }

    void tearDownDB();
}
