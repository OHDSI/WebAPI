package org.ohdsi.webapi.service;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "/application-test.properties")
public abstract class AbstractSpringBootServiceTest extends AbstractServiceTest {

}
