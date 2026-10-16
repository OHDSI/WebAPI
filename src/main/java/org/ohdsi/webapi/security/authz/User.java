package org.ohdsi.webapi.security.authz;
import org.ohdsi.webapi.security.authc.UserOrigin;

/**
 * API-facing User DTO (record). Entities are `*Entity` types; this record is
 * used when returning user data to callers. Permissions and index are
 * populated by callers.
 */
public record User(
    Long id,
    String login,
    String name
) {

  public static User fromEntity(UserEntity entity) {
    if (entity == null) return null;
    return new User(
        entity.getId(),
        entity.getLogin(),
        entity.getName()
    );
  }

  public static UserEntity toEntity(User user) {
    if (user == null) return null;
    UserEntity e = new UserEntity();
    e.setId(user.id());
    e.setLogin(user.login());
    e.setName(user.name());
    return e;
  }
}
