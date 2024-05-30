/*
 * Copyright 2024 cknoll1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.security;

import java.util.Arrays;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.dbunit.operation.DatabaseOperation;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

/**
 *
 * @author cknoll1
 */
@TestPropertySource(properties = {
  "security.provider=AtlasRegularSecurity"
})
public class PermissionTest extends AbstractDatabaseTest {
  
  @Autowired
  private PermissionManager permissionManager;

  @Value("${security.provider}")
  String securityProvider;

  @Autowired
  private DefaultWebSecurityManager securityManager;

  private Subject subject;
  
  @Before
  public void setup() {
    // Set the SecurityManager for the current thread
    SimplePrincipalCollection principalCollection = new SimplePrincipalCollection();
      principalCollection.addAll(Arrays.asList("permsTest"),"testRealm");
    subject = new Subject.Builder(securityManager)
            .authenticated(true)
            .principals(principalCollection)
            .buildSubject();
    ThreadContext.bind(subject);    
  }

  @Ignore
  @Test
  public void permsTest() throws Exception {
    // need to clear authorization cache before each test
    permissionManager.clearAuthorizationInfoCache();
    Subject s = SecurityUtils.getSubject();
    String subjetName = permissionManager.getSubjectName();

    final String[] testDataSetsPaths = new String[] {"/permission/permsTest_PREP.json" };
     
    loadPrepData(testDataSetsPaths, DatabaseOperation.REFRESH);

    // subject can manage printer1 and printer2, can do print and query on any printer.
    assertTrue(s.isPermitted("printer:manage:printer1"));
    assertTrue(s.isPermitted("printer:manage:printer2"));
    assertFalse(s.isPermitted("printer:manage:printer3"));
    assertTrue(s.isPermitted("printer:query:printer4"));
    assertTrue(s.isPermitted("printer:print:printer5"));
    
    loadPrepData(testDataSetsPaths, DatabaseOperation.DELETE);
    
  }
  
  @Ignore
  @Test
  public void wildcardTest() throws Exception {
    // need to clear authorization cache before each test
    permissionManager.clearAuthorizationInfoCache();
    Subject s = SecurityUtils.getSubject();
    final String[] testDataSetsPaths = new String[] {"/permission/wildcardTest_PREP.json" };
    loadPrepData(testDataSetsPaths, DatabaseOperation.REFRESH);

    // subject has * permisison, so any permisison test is true
    assertTrue(s.isPermitted("printer:manage:printer1"));
    assertTrue(s.isPermitted("printer"));
    
    loadPrepData(testDataSetsPaths, DatabaseOperation.DELETE);
    
  }

}
