package org.ohdsi.webapi.security.authc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OneTimeCodeServiceTest {

  private OneTimeCodeRepository repo;
  private OneTimeCodeService service;

  @BeforeEach
  void setUp() {
    repo = mock(OneTimeCodeRepository.class);
    service = new OneTimeCodeService(repo, new OneTimeCodeProperties());
  }

  @Test
  void returnsCodeWhenThisCallConsumedIt() {
    UUID code = UUID.randomUUID();
    OneTimeCodeEntity otc = new OneTimeCodeEntity();
    otc.setCode(code);
    otc.setLogin("alice");
    otc.setJwtToken("jwt");
    when(repo.consumeByCode(eq(code), any())).thenReturn(1);
    when(repo.findById(code)).thenReturn(Optional.of(otc));

    Optional<OneTimeCodeEntity> result = service.validateAndConsume(code);

    assertTrue(result.isPresent());
    assertEquals("jwt", result.get().getJwtToken());
  }

  @Test
  void rejectsCodeAlreadyConsumedByConcurrentRequest() {
    UUID code = UUID.randomUUID();
    // The conditional UPDATE matched no row: another request consumed it first,
    // or it is expired or unknown. The JWT must not be read or returned.
    when(repo.consumeByCode(eq(code), any())).thenReturn(0);

    Optional<OneTimeCodeEntity> result = service.validateAndConsume(code);

    assertTrue(result.isEmpty());
    verify(repo, never()).findById(code);
  }
}
