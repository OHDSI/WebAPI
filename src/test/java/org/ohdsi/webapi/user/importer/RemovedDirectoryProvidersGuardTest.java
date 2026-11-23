package org.ohdsi.webapi.user.importer;

import org.junit.Test;
import org.ohdsi.webapi.user.importer.repository.UserImportJobRepository;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RemovedDirectoryProvidersGuardTest {

  @Test
  public void enabledDirectoryAuthFailsSpringContextStartup() {
    for (String provider : new String[]{"ldap", "ad"}) {
      String property = "security.auth." + provider + ".enabled";
      UserImportJobRepository jobs = mock(UserImportJobRepository.class);
      try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
        context.setEnvironment(new MockEnvironment()
            .withProperty("security.provider", "DisabledSecurity")
            .withProperty(property, "true"));
        // Satisfy the production guard's explicit Flyway ordering without starting a database.
        context.getBeanFactory().registerSingleton("flyway", new Object());
        context.getBeanFactory().registerSingleton("userImportJobRepository", jobs);
        context.register(RemovedDirectoryProvidersGuard.class);
        try {
          context.refresh();
          fail("Enabled " + provider + " authentication must fail Spring startup");
        } catch (BeansException e) {
          Throwable cause = e;
          while (cause.getCause() != null) {
            cause = cause.getCause();
          }
          assertTrue(cause instanceof IllegalStateException);
          assertTrue(cause.getMessage().contains(property + "=false"));
        }
        verify(jobs, never()).countByEnabledTrueAndIsClosedFalse();
      }
    }
  }

  @Test
  public void rejectsLdapAndAdEvenWhenAnotherSecurityProviderIsSelected() {
    for (String provider : new String[]{"ldap", "ad"}) {
      String property = "security.auth." + provider + ".enabled";
      MockEnvironment environment = new MockEnvironment()
          .withProperty("security.provider", "DisabledSecurity")
          .withProperty(property, "true");
      try {
        new RemovedDirectoryProvidersGuard(environment, mock(UserImportJobRepository.class));
        fail("Removed provider must fail startup");
      } catch (IllegalStateException e) {
        assertTrue(e.getMessage().contains(property));
      }
    }
  }

  @Test
  public void rejectsActivePersistedJobs() {
    UserImportJobRepository jobs = mock(UserImportJobRepository.class);
    when(jobs.countByEnabledTrueAndIsClosedFalse()).thenReturn(1L);
    try {
      new RemovedDirectoryProvidersGuard(new MockEnvironment(), jobs).afterPropertiesSet();
      fail("Active import jobs must fail startup");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("Disable existing enabled, non-closed"));
    }
  }

  @Test
  public void permitsDisabledProvidersAndNoActiveJobs() {
    MockEnvironment environment = new MockEnvironment()
        .withProperty("security.auth.ldap.enabled", "false")
        .withProperty("security.auth.ad.enabled", "false");
    new RemovedDirectoryProvidersGuard(environment, mock(UserImportJobRepository.class)).afterPropertiesSet();
  }
}
