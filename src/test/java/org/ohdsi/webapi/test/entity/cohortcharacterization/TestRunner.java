package org.ohdsi.webapi.test.entity.cohortcharacterization;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.ohdsi.webapi.test.entity.cohortcharacterization.copy.TestEntityCopy;
import org.ohdsi.webapi.test.entity.cohortcharacterization.create.TestEntityCreate;
import org.ohdsi.webapi.test.entity.cohortcharacterization.importing.TestEntityImport;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestEntityCreate.class,
        TestEntityImport.class,
        TestEntityCopy.class
})
public class TestRunner {
}
