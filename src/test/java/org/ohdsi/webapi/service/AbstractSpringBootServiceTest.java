package org.ohdsi.webapi.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "/application-test.properties")
public abstract class AbstractSpringBootServiceTest extends AbstractServiceTest {

}
