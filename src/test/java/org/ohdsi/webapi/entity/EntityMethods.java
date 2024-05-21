package org.ohdsi.webapi.entity;

import org.ohdsi.webapi.CommonDTO;
import org.springframework.test.context.TestContextManager;

public interface EntityMethods<T extends CommonDTO> {

    void initFirstDTO() throws Exception;

    T createEntity(String name) throws Exception;

    default void init() throws Exception {

        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        initFirstDTO();
    }

    void tearDownDB();
}
