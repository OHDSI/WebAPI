package org.ohdsi.webapi.mvc.controller;

import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.security.AccessType;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.security.dto.AccessRequestDTO;
import org.ohdsi.webapi.security.dto.RoleDTO;
import org.ohdsi.webapi.security.model.EntityType;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Spring MVC version of PermissionController
 *
 * Migration Status: Replaces /security/PermissionController.java (Jersey)
 * Endpoints: 6 endpoints (3 GET, 1 POST, 1 DELETE, 1 GET with query)
 * Complexity: Medium - permission management and authorization logic
 */
@RestController
@RequestMapping("/permission")
@Transactional
public class PermissionMvcController extends AbstractMvcController {

    private final PermissionService permissionService;
    private final PermissionManager permissionManager;
    private final ConversionService conversionService;

    public PermissionMvcController(
            PermissionService permissionService,
            PermissionManager permissionManager,
            @Qualifier("conversionService") ConversionService conversionService) {
        this.permissionService = permissionService;
        this.permissionManager = permissionManager;
        this.conversionService = conversionService;
    }

    /**
     * Get the list of permissions for a user
     *
     * Jersey: GET /WebAPI/permission
     * Spring MVC: GET /WebAPI/v2/permission
     *
     * @return A list of permissions
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserService.Permission>> getPermissions() {
        Iterable<PermissionEntity> permissionEntities = permissionManager.getPermissions();
        List<UserService.Permission> permissions = StreamSupport.stream(permissionEntities.spliterator(), false)
            .map(UserService.Permission::new)
            .collect(Collectors.toList());
        return ok(permissions);
    }

    /**
     * Get the roles matching the roleSearch value
     *
     * Jersey: GET /WebAPI/permission/access/suggest?roleSearch={roleSearch}
     * Spring MVC: GET /WebAPI/v2/permission/access/suggest?roleSearch={roleSearch}
     *
     * @summary Role search
     * @param roleSearch The role to search
     * @return The list of roles
     */
    @GetMapping(
        value = "/access/suggest",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<RoleDTO>> listAccessesForEntity(@RequestParam("roleSearch") String roleSearch) {
        List<org.ohdsi.webapi.shiro.Entities.RoleEntity> roles = permissionService.suggestRoles(roleSearch);
        List<RoleDTO> roleDTOs = roles.stream()
            .map(re -> conversionService.convert(re, RoleDTO.class))
            .collect(Collectors.toList());
        return ok(roleDTOs);
    }

    /**
     * Get roles that have a permission type (READ/WRITE) to entity
     *
     * Jersey: GET /WebAPI/permission/access/{entityType}/{entityId}/{permType}
     * Spring MVC: GET /WebAPI/v2/permission/access/{entityType}/{entityId}/{permType}
     *
     * @summary Get roles that have a specific permission (READ/WRITE) for the entity
     * @param entityType The entity type
     * @param entityId The entity ID
     * @param permType The permission type
     * @return The list of permissions for the permission type
     * @throws Exception
     */
    @GetMapping(
        value = "/access/{entityType}/{entityId}/{permType}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<RoleDTO>> listAccessesForEntityByPermType(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Integer entityId,
            @PathVariable("permType") AccessType permType) throws Exception {
        permissionService.checkCommonEntityOwnership(entityType, entityId);
        var permissionTemplates = permissionService.getTemplatesForType(entityType, permType).keySet();

        List<String> permissions = permissionTemplates.stream()
                .map(pt -> permissionService.getPermission(pt, entityId))
                .collect(Collectors.toList());

        List<org.ohdsi.webapi.shiro.Entities.RoleEntity> roles = permissionService.finaAllRolesHavingPermissions(permissions);

        List<RoleDTO> roleDTOs = roles.stream()
            .map(re -> conversionService.convert(re, RoleDTO.class))
            .collect(Collectors.toList());
        return ok(roleDTOs);
    }

    /**
     * Get roles that have a permission type (READ/WRITE) to entity
     *
     * Jersey: GET /WebAPI/permission/access/{entityType}/{entityId}
     * Spring MVC: GET /WebAPI/v2/permission/access/{entityType}/{entityId}
     *
     * @summary Get roles that have a specific permission (READ/WRITE) for the entity
     * @param entityType The entity type
     * @param entityId The entity ID
     * @return The list of permissions for the permission type
     * @throws Exception
     */
    @GetMapping(
        value = "/access/{entityType}/{entityId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<RoleDTO>> listAccessesForEntity(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Integer entityId) throws Exception {
        return listAccessesForEntityByPermType(entityType, entityId, AccessType.WRITE);
    }

    /**
     * Grant group of permissions (READ / WRITE / ...) for the specified entity to the given role.
     * Only owner of the entity can do that.
     *
     * Jersey: POST /WebAPI/permission/access/{entityType}/{entityId}/role/{roleId}
     * Spring MVC: POST /WebAPI/v2/permission/access/{entityType}/{entityId}/role/{roleId}
     *
     * @summary Grant permissions
     * @param entityType The entity type
     * @param entityId The entity ID
     * @param roleId The role ID
     * @param accessRequestDTO The access request object
     * @throws Exception
     */
    @PostMapping(
        value = "/access/{entityType}/{entityId}/role/{roleId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> grantEntityPermissionsForRole(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Integer entityId,
            @PathVariable("roleId") Long roleId,
            @RequestBody AccessRequestDTO accessRequestDTO) throws Exception {
        permissionService.checkCommonEntityOwnership(entityType, entityId);

        var permissionTemplates = permissionService.getTemplatesForType(entityType, accessRequestDTO.getAccessType());

        org.ohdsi.webapi.shiro.Entities.RoleEntity role = permissionManager.getRole(roleId);
        permissionManager.addPermissionsFromTemplate(role, permissionTemplates, entityId.toString());

        return ok();
    }

    /**
     * Remove group of permissions for the specified entity to the given role.
     *
     * Jersey: DELETE /WebAPI/permission/access/{entityType}/{entityId}/role/{roleId}
     * Spring MVC: DELETE /WebAPI/v2/permission/access/{entityType}/{entityId}/role/{roleId}
     *
     * @summary Remove permissions
     * @param entityType The entity type
     * @param entityId The entity ID
     * @param roleId The role ID
     * @param accessRequestDTO The access request object
     * @throws Exception
     */
    @DeleteMapping(
        value = "/access/{entityType}/{entityId}/role/{roleId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> revokeEntityPermissionsFromRole(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Integer entityId,
            @PathVariable("roleId") Long roleId,
            @RequestBody AccessRequestDTO accessRequestDTO) throws Exception {
        permissionService.checkCommonEntityOwnership(entityType, entityId);
        var permissionTemplates = permissionService.getTemplatesForType(entityType, accessRequestDTO.getAccessType());
        permissionService.removePermissionsFromRole(permissionTemplates, entityId, roleId);

        return ok();
    }
}
