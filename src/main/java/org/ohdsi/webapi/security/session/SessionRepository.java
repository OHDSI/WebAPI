package org.ohdsi.webapi.security.session;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {

  @Query("""
          select count(s) > 0
          from Session s
          where s.login = :login
            and s.sessionId = :sessionId
            and s.revoked = false
            and s.expiresAt > :now
      """)
  boolean isSessionValid(
      String login,
      UUID sessionId,
      Instant now);

  @Modifying
  @Query("""
          update Session s
          set s.revoked = true
          where s.login = :login
      """)
  void revokeByUsername(String login);

  @Modifying
  @Query("""
          update Session s
          set s.revoked = true
          where s.sessionId = :sessionId
      """)
  void revokeBySessionId(UUID sessionId);

  @Modifying
  @Query("""
          delete from Session s
          where s.expiresAt < :now
      """)
  void deleteByExpiresAtBefore(Instant now);

    @Query("""
        select count(s)
        from Session s
        where s.expiresAt < :now
    """)
    long countByExpiresAtBefore(Instant now);

    @Query("""
        select count(s)
        from Session s
        where s.expiresAt > :now
    """)
    long countByExpiresAtAfter(Instant now);    

}
