package org.ohdsi.webapi.security.authz;

import java.util.List;

import org.ohdsi.webapi.security.authz.access.AccessRequestDTO;
import org.ohdsi.webapi.security.authz.access.AccessType;
import org.ohdsi.webapi.security.authz.access.EntityType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;


/**
 * REST Services related to working with security permissions.
 *
 * Endpoints under /permission manage global permissions and
 * entity-level access grants (sec_{entity} tables).
 *
 * @summary Permissions
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    private final AuthorizationService authorizationService;

    public PermissionController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * Get all global permissions defined in the system.
     *
     * @return A list of permissions
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isPermitted('list')")
    public List<Permission> getPermissions() {
        return this.authorizationService.getPermissions();
    }

    /**
     * Search for roles matching the roleSearch value.
     *
     * @summary Role search
     * @param roleSearch The partial role name to search for (case-insensitive), or empty for all
     * @return The list of matching roles
     */
    @GetMapping(value = "/access/suggest", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isPermitted('admin:security')")
    public List<Role> suggestRoles(@RequestParam(value = "roleSearch", required = false) String roleSearch) {
        return this.authorizationService.searchRoles(roleSearch);
    }

    /**
     * Get roles that have a specific access type (READ/WRITE) to an entity.
     *
     * @summary Get roles with entity access by type
     * @param entityType The entity type
     * @param entityId   The entity ID
     * @param accessType The access type (READ or WRITE)
     * @return The list of roles with the specified access
     */
    @GetMapping(value = "/access/{entityType}/{entityId}/{accessType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isPermitted('admin:security')")
    public List<Role> getRolesForEntityByAccessType(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Long entityId,
            @PathVariable("accessType") AccessType accessType
    ) {
        return this.authorizationService.getRolesForEntity(entityType, entityId, accessType);
    }

    /**
     * Get roles that have WRITE access to an entity (convenience endpoint).
     *
     * @summary Get roles with WRITE access to entity
     * @param entityType The entity type
     * @param entityId   The entity ID
     * @return The list of roles with WRITE access
     */
    @GetMapping(value = "/access/{entityType}/{entityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isPermitted('admin:security')")
    public List<Role> getRolesForEntity(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Long entityId
    ) {
        return getRolesForEntityByAccessType(entityType, entityId, AccessType.WRITE);
    }

    /**
     * Grant entity access (READ/WRITE) to a role.
     *
     * @summary Grant entity access
     * @param entityType       The entity type
     * @param entityId         The entity ID
     * @param roleId           The role ID
     * @param accessRequestDTO The access request containing the access type
     */
    @PostMapping(value = "/access/{entityType}/{entityId}/role/{roleId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#entityId, #entityType) or isPermitted('admin:security')")
    public void grantEntityAccess(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Long entityId,
            @PathVariable("roleId") Long roleId,
            @RequestBody AccessRequestDTO accessRequestDTO
    ) {
        this.authorizationService.grantEntityAccess(entityType, entityId, roleId, accessRequestDTO.getAccessType());
    }

    /**
     * Revoke entity access from a role.
     *
     * @summary Revoke entity access
     * @param entityType       The entity type
     * @param entityId         The entity ID
     * @param roleId           The role ID
     * @param accessRequestDTO The access request containing the access type to revoke
     */
    @DeleteMapping(value = "/access/{entityType}/{entityId}/role/{roleId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#entityId, #entityType) or isPermitted('admin:security')")
    public void revokeEntityAccess(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Long entityId,
            @PathVariable("roleId") Long roleId,
            @RequestBody AccessRequestDTO accessRequestDTO
    ) {
        this.authorizationService.revokeEntityAccess(entityType, entityId, roleId, accessRequestDTO.getAccessType());
    }
}
