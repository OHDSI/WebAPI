package org.ohdsi.webapi.user.importer;

import org.ohdsi.webapi.user.importer.repository.UserImportJobRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Reject obsolete configuration without changing persisted directory-import data. */
@Component
@DependsOn("flyway")
public class RemovedDirectoryProvidersGuard implements InitializingBean {

  private final UserImportJobRepository jobs;

  public RemovedDirectoryProvidersGuard(Environment environment, UserImportJobRepository jobs) {
    rejectEnabledProvider(environment, "security.auth.ldap.enabled");
    rejectEnabledProvider(environment, "security.auth.ad.enabled");
    this.jobs = jobs;
  }

  private static void rejectEnabledProvider(Environment environment, String property) {
    if (environment.getProperty(property, Boolean.class, false)) {
      throw new IllegalStateException("LDAP/AD authentication has been removed; set " + property + "=false.");
    }
  }

  @Override
  public void afterPropertiesSet() {
    if (jobs.countByEnabledTrueAndIsClosedFalse() > 0) {
      throw new IllegalStateException("Directory imports have been removed. Disable existing enabled, non-closed "
          + "user import jobs before starting WebAPI; job data and history are retained.");
    }
  }
}
