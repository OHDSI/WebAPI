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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;
import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.util.TableFormatter;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.security.authc.AuthenticatedLogin;
import org.ohdsi.webapi.security.authc.LoginController;
import org.ohdsi.webapi.security.authc.LoginService;
import org.ohdsi.webapi.security.authz.mapping.OidcGroupToRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.junit.Assert.*;

/**
 * Integration tests for External Role Mapping subsystem - Login Flow.
 * 
 * Tests verify that the real LoginController methods properly invoke the login
 * flow, which syncs user roles with external role mappings. Tests compare actual
 * database state (sec_user_role) against expected state using DBUnit.
 * 
 * Test flow:
 * 1. Load test data via DatabaseOperation.INSERT
 * 2. Call LoginController method with mocked Authentication
 * 3. Query actual sec_user_role table state
 * 4. Load expected state from fixture
 * 5. Use DBUnit Assertion.assertEquals() to verify
 * 6. Cleanup via DatabaseOperation.DELETE (cascade removes sec_user_role entries)
 * 
 * @author cknoll1
 */
public class ExternalRoleMappingTest extends AbstractDatabaseTest {
  
  @Autowired
  private LoginService loginService;
  
  @Autowired
  private org.ohdsi.webapi.security.authz.mapping.ExternalRoleMapService externalRoleMapService;

  private static final String SETUP_DATA = "/externalRoleMapping/externalRoleMapping_SETUP.json";
  private static final String EXPECTED_DATA = "/externalRoleMapping/externalRoleMapping_EXPECTED.json";

  /**
   * Test 1: LDAP Login - Role Assignment
   * 
   * Scenario: User logs in with LDAP group; role is synchronized to database
   * - ldap_user starts with NO roles
   * - Login with CN=Analysts group (mapped to Analyst role)
   * - Expected: Analyst role assigned with origin='LDAP'
   */
  @Test
  public void testRoleMapping() throws Exception {
    // contains 3 users with default roles
    loadPrepData(new String[] { SETUP_DATA }, DatabaseOperation.INSERT);
    
    GrantedAuthority authority = null;
    TestingAuthenticationToken mockAuth = null;
    LoginService.Result loginResult = null;

    try {
      // Case 1: ldap user with no roles should have roles added
      // Setup: Mock LDAP authentication with CN=Analysts group
      authority = new SimpleGrantedAuthority("CN=Analysts,OU=Groups,DC=corp,DC=local");
      mockAuth = new TestingAuthenticationToken("ldap_user", null, Arrays.asList(authority));
      mockAuth.setAuthenticated(true);
      
      // Action: Call LoginController.Ldap.login()
      // This invokes the real login flow: mapper resolves group → role → syncRoles updates DB
      LoginController.Ldap ldapController = new LoginController.Ldap(loginService, externalRoleMapService);
      loginResult = ldapController.login(mockAuth);
      
      assertNotNull("Login should succeed", loginResult);
      assertEquals("Login should return ldap_user", "ldap_user", loginResult.login());
      
      // case 2: Windows User has role added and removed
      // Setup: Mock Windows authentication with CORP\Designers group
      authority = new SimpleGrantedAuthority("CORP\\Designers");
      mockAuth = new TestingAuthenticationToken("windows_user", null, Arrays.asList(authority));
      mockAuth.setAuthenticated(true);
      
      // Action: Call LoginController.Windows.login()
      // This invokes the real login flow: mapper resolves group → role → syncRoles updates DB
      LoginController.Windows windowsController = new LoginController.Windows(loginService, externalRoleMapService);
      loginResult = windowsController.login(mockAuth);
      
      assertNotNull("Login should succeed", loginResult);
      assertEquals("Login should return windows_user", "windows_user", loginResult.login());

      // Case 3: OIDC user has no changes, move complicated since it doesn't use a controller in this case:
      // (OidcAuthConfig.handleSuccess() does: extract roles via mapper → call onSuccess), which we re-create here     
      // Mock mapper resolves realm_analyst → Analyst role
      OidcGroupToRoleMapper oidcMapper = new OidcGroupToRoleMapper(externalRoleMapService);
      
      Map<String, Object> claims = new HashMap<>();
      Map<String, Object> realmAccess = new HashMap<>();
      realmAccess.put("roles", Arrays.asList("realm_analyst"));
      claims.put("realm_access", realmAccess);
      
      Set<String> mappedRoles = oidcMapper.extractAndMapRoles(claims, "realm_access.roles", false);
      assertEquals("Should resolve realm_analyst to Analyst role", 1, mappedRoles.size());
      assertTrue("Should contain Analyst", mappedRoles.contains("Analyst"));
      
      // Action: Call LoginService.onSuccess() to simulate OIDC login completion
      AuthenticatedLogin authenticatedLogin = AuthenticatedLogin.builder()
          .login("oidc_user")
          .name("OIDC Test User")
          .origin(org.ohdsi.webapi.security.authc.UserOrigin.OIDC)
          .roles(mappedRoles)
          .originAuthentication(null)
          .build();
      
      LoginService.Result result = loginService.onSuccess(authenticatedLogin);
      
      assertNotNull("Login should succeed", result);
      assertEquals("Login should return oidc_user", "oidc_user", result.login());

      // Verification: Compare actual vs expected sec_user_role
      IDatabaseConnection dbUnitCon = getConnection();
      try {
        // Query actual sec_user_role table, looking only for data involved in this test
        ITable actualTable = dbUnitCon.createQueryTable(
          "expected.mapping",
          "SELECT distinct user_id, role_id, origin FROM public.sec_user_role WHERE user_id >= 50000 and origin <> 'SYSTEM' ORDER BY user_id, role_id, origin"
        );

        TableFormatter f = new TableFormatter();
        String resultsTableText = f.format(actualTable);        
        
        // Load expected from consolidated fixture and get LDAP table
        IDataSet expectedDataSet = DataSetFactory.createDataSet(new String[] { EXPECTED_DATA });
        ITable expectedTable = expectedDataSet.getTable("expected.mapping");
        Assertion.assertEquals(expectedTable, actualTable);
      } finally {
        dbUnitCon.close();
      }
    } finally { 
      // Need to cleanup sec_user_role manually because test created new data
      jdbcTemplate.execute(String.format("DELETE FROM %s WHERE user_id >= %d",ohdsiSchema + ".sec_user_role", 50000));
      loadPrepData(new String[] { SETUP_DATA }, DatabaseOperation.DELETE);
    }
  }
}
