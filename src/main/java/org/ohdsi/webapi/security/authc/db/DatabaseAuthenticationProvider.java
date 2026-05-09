package org.ohdsi.webapi.security.authc.db;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DatabaseAuthenticationProvider implements AuthenticationProvider {

  private static final Logger log = LoggerFactory.getLogger(DatabaseAuthenticationProvider.class);

  private final DatabaseUserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;
  private final LockoutPolicyProperties lockoutProps;

  public DatabaseAuthenticationProvider(DatabaseUserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder,
      LockoutPolicyProperties lockoutProps) {
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
    this.lockoutProps = lockoutProps;
  }

  @Override
  public Authentication authenticate(Authentication authentication) {
    String login = authentication.getName();
    String password = (String) authentication.getCredentials();

    DatabaseUser user = userDetailsService.loadUserByLogin(login);

    if (user == null || !user.enabled()) {
      throw new DisabledException("User not found or disabled");
    }

    if (user.isAccountLocked()) {
      throw new LockedException("Account locked until " + user.lockedUntil());
    }

    if (!passwordEncoder.matches(password, user.passwordHash())) {
      userDetailsService.incrementFailedAttempts(login);
      if (user.failedAttempts() + 1 >= lockoutProps.getMaxFailedAttempts()) {
        userDetailsService.lockUser(login, LocalDateTime.now().plus(lockoutProps.getLockoutDuration()));
      }
      throw new BadCredentialsException("Invalid credentials");
    }

    log.info("Successful login for DB authentication.  Resetting failed attempts and returning authenticated token");
    // Successful login → reset failed attempts
    userDetailsService.resetFailedAttempts(login);

    return new UsernamePasswordAuthenticationToken(
        login,
        null,
        user.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
