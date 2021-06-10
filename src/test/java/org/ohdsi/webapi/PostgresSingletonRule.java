package org.ohdsi.webapi;

/*
 * Copyright 2020 cknoll1.
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
 /*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.rules.ExternalResource;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based off of com.opentable.db.postgres.junit.SingleInstancePostgresRule, but
 * instantiates a single instance of an EmbeddedPostgres that cleans up when JVM
 * shuts down.
 */
public class PostgresSingletonRule extends ExternalResource {

  private static volatile EmbeddedPostgres epg;
  private static volatile Connection postgresConnection;
  private static final Logger LOG = LoggerFactory.getLogger(PostgresSingletonRule.class);

  PostgresSingletonRule() {}

  @Override
  protected void before() throws Throwable {
    super.before();
    synchronized (PostgresSingletonRule.class) {
      if (epg == null) {
        LOG.info("Starting singleton Postgres instance...");
        epg = pg();
        postgresConnection = epg.getPostgresDatabase().getConnection();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));        
      }
    }
  }

  private EmbeddedPostgres pg() throws IOException {
    return EmbeddedPostgres.builder().start();
  }

  public EmbeddedPostgres getEmbeddedPostgres() {
    EmbeddedPostgres epg = this.epg;
    if (epg == null) {
      throw new AssertionError("JUnit tests not started yet!");
    }
    return epg;
  }

  private static void shutdown() {
    LOG.info("Shutdown singleton Postgres instance...");
    try {
      postgresConnection.close();
    } catch (SQLException e) {
      throw new AssertionError(e);
    }
    try {
      epg.close();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }
}
