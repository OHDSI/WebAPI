package org.ohdsi.webapi.security.authz;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * The PermissionService is intentionaly left package-protected, as all
 * interactions with authz will be performed through AuthorizationService.
 * 
 * PermissionService manages roll lifecycle operations (including enforcing
 * permisson format values) and lookup functions.
 * Making this service a package-protected class will let us return JPA Entities
 * freely without risking leaking entities to outer callers.
 */
@Service
@Transactional
class PermissionService {

  private final PermissionRepository permissionRepository;

  public PermissionService(PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  public PermissionEntity getOrAddPermission(final String permissionName, final String permissionDescription) {
    Assert.hasLength(permissionName, "permissionName can not be empty.");

    return this.permissionRepository.findByValueIgnoreCase(permissionName)
        .orElseGet(() -> {
          PermissionEntity newPermission = new PermissionEntity();
          newPermission.setValue(permissionName);
          newPermission.setDescription(permissionDescription);
          newPermission = this.permissionRepository.save(newPermission);
          return newPermission;

        });
  }

  public Iterable<PermissionEntity> getPermissions() {
    return this.permissionRepository.findAll();
  }

  public void removePermission(String value) {
    this.permissionRepository.findByValueIgnoreCase(value)
        .ifPresent((p) -> this.permissionRepository.delete(p));
  }

  public PermissionEntity getPermissionById(Long permissionId) {
    return this.permissionRepository.findById(permissionId).orElseThrow();
  }
}
