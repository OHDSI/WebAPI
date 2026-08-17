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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Concurrent first logins for the same principal must all succeed. sec_user.login is
 * unique, so only one of them can insert the user and the rest have to fall back to it.
 */
public class UserRegistrationRaceTest extends AbstractDatabaseTest {

  @Autowired
  private AuthorizationService authorizationService;

  private static final String LOGIN = "race_test_user";
  private static final int THREADS = 16;

  @After
  public void deleteFixture() {
    jdbcTemplate.update("DELETE FROM " + ohdsiSchema + ".sec_user_role WHERE user_id IN "
        + "(SELECT id FROM " + ohdsiSchema + ".sec_user WHERE login = ?)", LOGIN);
    jdbcTemplate.update("DELETE FROM " + ohdsiSchema + ".sec_user WHERE login = ?", LOGIN);
    jdbcTemplate.update("DELETE FROM " + ohdsiSchema + ".sec_role WHERE name = ?", LOGIN);
  }

  @Test
  public void testConcurrentFirstLoginsAllSucceed() throws Exception {
    CyclicBarrier startTogether = new CyclicBarrier(THREADS);
    ExecutorService pool = Executors.newFixedThreadPool(THREADS);

    try {
      List<Callable<Object>> logins = IntStream.range(0, THREADS)
          .<Callable<Object>>mapToObj(i -> () -> {
            startTogether.await(30, TimeUnit.SECONDS);
            return authorizationService.ensureUserExists(LOGIN, LOGIN, UserOrigin.OIDC, List.of());
          })
          .collect(Collectors.toList());

      List<Future<Object>> results = pool.invokeAll(logins, 60, TimeUnit.SECONDS);

      for (Future<Object> result : results) {
        try {
          result.get();
        } catch (Exception e) {
          fail("Concurrent first login failed: " + e.getCause());
        }
      }
    } finally {
      pool.shutdownNow();
    }

    assertEquals("Exactly one user should have been registered", 1,
        (int) jdbcTemplate.queryForObject(
            "SELECT count(*) FROM " + ohdsiSchema + ".sec_user WHERE login = ?", Integer.class, LOGIN));
  }
}
