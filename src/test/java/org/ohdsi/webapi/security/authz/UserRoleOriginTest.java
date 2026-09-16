/*
 * Copyright 2026 p-hoffmann.
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
package org.ohdsi.webapi.security.authz;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

/**
 * Verifies that a role assignment is tracked per authentication origin, so a grant from
 * one origin neither blocks nor is blocked by the same role granted from another.
 */
public class UserRoleOriginTest extends AbstractDatabaseTest {

  @Autowired
  private RoleService roleService;

  @Autowired
  private UserService userService;

  private static final Long USER_ID = 51001L;
  private static final Long ROLE_ID = 51002L;
  private static final String LOGIN = "origin_test_user";
  private static final String ROLE_NAME = "OriginTestRole";

  @Before
  public void insertFixture() {
    deleteFixture();
    jdbcTemplate.update("INSERT INTO " + ohdsiSchema + ".sec_user (id, login, name, origin) VALUES (?, ?, ?, 'SYSTEM')",
        USER_ID, LOGIN, LOGIN);
    jdbcTemplate.update("INSERT INTO " + ohdsiSchema + ".sec_role (id, name, system_role) VALUES (?, ?, true)",
        ROLE_ID, ROLE_NAME);
  }

  @After
  public void deleteFixture() {
    jdbcTemplate.update("DELETE FROM " + ohdsiSchema + ".sec_user_role WHERE user_id = ? OR role_id = ?",
        USER_ID, ROLE_ID);
    jdbcTemplate.update("DELETE FROM " + ohdsiSchema + ".sec_user WHERE id = ?", USER_ID);
    jdbcTemplate.update("DELETE FROM " + ohdsiSchema + ".sec_role WHERE id = ?", ROLE_ID);
  }

  private int countAssignments(String origin) {
    String sql = "SELECT count(*) FROM " + ohdsiSchema + ".sec_user_role WHERE user_id = ? AND role_id = ?"
        + (origin == null ? "" : " AND origin = '" + origin + "'");
    return jdbcTemplate.queryForObject(sql, Integer.class, USER_ID, ROLE_ID);
  }

  private void insertAssignment(String origin) {
    jdbcTemplate.update("INSERT INTO " + ohdsiSchema + ".sec_user_role (id, user_id, role_id, origin) "
        + "VALUES (nextval('" + ohdsiSchema + ".sec_user_role_sequence'), ?, ?, ?)", USER_ID, ROLE_ID, origin);
  }

  @Test
  public void testGrantFromSecondOriginIsNotShadowed() {
    UserEntity user = userService.getUserById(USER_ID);
    RoleEntity role = roleService.getRole(ROLE_ID);

    roleService.addUserToRole(user, role, UserOrigin.SYSTEM);
    roleService.addUserToRole(user, role, UserOrigin.OIDC);

    assertEquals("SYSTEM grant should be recorded", 1, countAssignments("SYSTEM"));
    assertEquals("OIDC grant must not be shadowed by the existing SYSTEM grant", 1, countAssignments("OIDC"));

    roleService.addUserToRole(user, role, UserOrigin.OIDC);
    assertEquals("Re-granting the same origin should not duplicate", 1, countAssignments("OIDC"));
  }

  @Test
  public void testRemoveByOriginLeavesOtherOriginsIntact() {
    UserEntity user = userService.getUserById(USER_ID);
    RoleEntity role = roleService.getRole(ROLE_ID);

    roleService.addUserToRole(user, role, UserOrigin.SYSTEM);
    roleService.addUserToRole(user, role, UserOrigin.OIDC);

    roleService.removeUserFromRole(LOGIN, ROLE_NAME, UserOrigin.OIDC);

    assertEquals("OIDC grant should be removed", 0, countAssignments("OIDC"));
    assertEquals("SYSTEM grant should survive", 1, countAssignments("SYSTEM"));

    roleService.removeUser(USER_ID, ROLE_ID);
    assertEquals("Removing the user from the role should clear every origin", 0, countAssignments(null));
  }

  @Test
  public void testDuplicateRowsDoNotBreakAssignment() {
    UserEntity user = userService.getUserById(USER_ID);
    RoleEntity role = roleService.getRole(ROLE_ID);

    // Duplicates predating the dedupe migration must not make the lookup throw.
    insertAssignment("SYSTEM");
    insertAssignment("SYSTEM");

    roleService.addUserToRole(user, role, UserOrigin.SYSTEM);
    assertEquals("Existing duplicates should be left alone, not added to", 2, countAssignments("SYSTEM"));

    roleService.removeUser(USER_ID, ROLE_ID);
    assertEquals("Removal should clear duplicates too", 0, countAssignments(null));
  }
}
