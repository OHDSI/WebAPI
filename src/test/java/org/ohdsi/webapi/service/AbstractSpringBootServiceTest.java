package org.ohdsi.webapi.service;

import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "/application-test.properties")
public abstract class AbstractSpringBootServiceTest extends AbstractServiceTest {

    // Use the same embedded Postgres setup as AbstractDatabaseTest
    @ClassRule
    public static TestRule chain = AbstractDatabaseTest.chain;

    @DynamicPropertySource
    static void configureTestDatabase(DynamicPropertyRegistry registry) {
        AbstractDatabaseTest.configureTestDatabase(registry);
    }

}
