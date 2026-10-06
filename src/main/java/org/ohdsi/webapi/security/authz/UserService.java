package org.ohdsi.webapi.security.authz;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The UserService is intentionaly left package-protected, as all interactions with authz will be performed through AuthorizationService.
 * 
 * UserService manages user lifecycle operations (including user creation and personal role assignment) and user lookup.
 * Making this service a package-protected class will let us return JPA Entities freely without risking leaking entities to outer callers.
 * Because of the 'personal role' being a user invariant, this service touches the role and user-role repositories.
 */
@Service
class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // -------------------------
  // Lookup Operations
  // -------------------------
  @Transactional(readOnly = true)
  public UserEntity getUserById(Long userId) {
    return this.userRepository.findById(userId).orElseThrow();
  }

  @Transactional(readOnly = true)
  public Optional<UserEntity> getUserByLogin(final String login) {
    return this.userRepository.findByLogin(login);
  }

  /**
   * Save the user to the user repository.
   * @param user
   * @return The saved user entity.
   */
  @Transactional
  public UserEntity save(UserEntity user) {
    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public Iterable<UserEntity> getAllUsers() {
    return this.userRepository.findAll();
  }
}
