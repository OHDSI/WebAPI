package org.ohdsi.webapi.test.entity;

import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public class BaseTestEntity {    
    @Autowired
    protected ConversionService conversionService;
    protected final static String COPY_PREFIX = "COPY OF: ";
    protected final static String NEW_TEST_ENTITY = "New test entity";
    protected final static String SOME_UNIQUE_TEST_NAME = "Some unique test name";
}
