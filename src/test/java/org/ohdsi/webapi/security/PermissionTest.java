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

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.dbunit.operation.DatabaseOperation;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.security.authc.WebApiAuthenticationToken;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.User;
import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.ohdsi.webapi.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

/**
 *
 * @author cknoll1
 */
public class PermissionTest extends AbstractDatabaseTest {
  
  @Autowired
  private AuthorizationService authorizationService;

  @Before
  public void setup() {
    // Set the Principal for the current thread
    WebApiPrincipal principal = new WebApiPrincipal(new User(100001L, "permsTest", "Permission Test"));
    Authentication auth = WebApiAuthenticationToken.authenticated(principal, UUID.randomUUID(), Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  @Ignore("Database schema issue: public.sec_user table not properly initialized")
  public void permsTest() throws Exception {
    // need to clear authorization cache before each test
    authorizationService.clearCache();

    final String[] testDataSetsPaths = new String[] {"/permission/permsTest_PREP.json" };
     
    loadPrepData(testDataSetsPaths, DatabaseOperation.REFRESH);

    // subject can manage printer1 and printer2, can do print and query on any printer.
    assertTrue(authorizationService.isPermitted("printer:manage:printer1"));
    assertTrue(authorizationService.isPermitted("printer:manage:printer2"));
    assertFalse(authorizationService.isPermitted("printer:manage:printer3"));
    assertTrue(authorizationService.isPermitted("printer:query:printer4"));
    assertTrue(authorizationService.isPermitted("printer:print:printer5"));
    
    loadPrepData(testDataSetsPaths, DatabaseOperation.DELETE);
    
  }
  
  @Test
  @Ignore("Database schema issue: public.sec_user table not properly initialized")
  public void wildcardTest() throws Exception {
    // need to clear authorization cache before each test
    authorizationService.clearCache();

    final String[] testDataSetsPaths = new String[] {"/permission/wildcardTest_PREP.json" };
    loadPrepData(testDataSetsPaths, DatabaseOperation.REFRESH);

    // subject has * permisison, so any permisison test is true
    assertTrue(authorizationService.isPermitted("printer:manage:printer1"));
    assertTrue(authorizationService.isPermitted("printer"));
    
    loadPrepData(testDataSetsPaths, DatabaseOperation.DELETE);
    
  }

}
