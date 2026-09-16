package org.ohdsi.webapi.security.authc.db;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

public record DatabaseUser(
    String username,
    String passwordHash,
    String firstName,
    String middleName,
    String lastName,
    boolean enabled,
    int failedAttempts,
    LocalDateTime lockedUntil) {

  public boolean isAccountLocked() {
    return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
  }

  public Collection<GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
  }

}
