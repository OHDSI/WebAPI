package org.ohdsi.webapi.security;

import org.ohdsi.webapi.security.dto.AccessRequestDTO;
import org.ohdsi.webapi.security.dto.RoleDTO;
import org.ohdsi.webapi.security.model.EntityType;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * REST Services related to working with security permissions
 * 
 * @summary Notifications
 */
@Controller
@Path(value = "/permission")
@Transactional
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionManager permissionManager;
    private final ConversionService conversionService;

    public PermissionController(PermissionService permissionService, PermissionManager permissionManager, ConversionService conversionService) {

        this.permissionService = permissionService;
        this.permissionManager = permissionManager;
        this.conversionService = conversionService;
    }

    /**
     * Get the list of permissions for a user
     * 
     * @return A list of permissions
     */
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserService.Permission> getPermissions() {

        Iterable<PermissionEntity> permissionEntities = this.permissionManager.getPermissions();
        return StreamSupport.stream(permissionEntities.spliterator(), false)
            .map(UserService.Permission::new)
            .collect(Collectors.toList());
    }

    /**
     * Get the roles matching the roleSearch value
     * 
     * @summary Role search
     * @param roleSearch The role to search
     * @return The list of roles
     */
    @GET
    @Path("/access/suggest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RoleDTO> listAccessesForEntity(@QueryParam("roleSearch") String roleSearch) {

        List<RoleEntity> roles = permissionService.suggestRoles(roleSearch);
        return roles.stream().map(re -> conversionService.convert(re, RoleDTO.class)).collect(Collectors.toList());
    }

    /**
     * Get roles that have a permission type (READ/WRITE) to entity
     *
     * @summary Get roles that have a specific permission (READ/WRITE) for the
     * entity
     * @param entityType The entity type
     * @param entityId The entity ID
     * @return The list of permissions for the permission type
     * @throws Exception
     */
    @GET
    @Path("/access/{entityType}/{entityId}/{permType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RoleDTO> listAccessesForEntityByPermType(
            @PathParam("entityType") EntityType entityType,
            @PathParam("entityId") Integer entityId,
            @PathParam("permType") AccessType permType
    ) throws Exception {

        permissionService.checkCommonEntityOwnership(entityType, entityId);
        Set<String> permissionTemplates = null;
        permissionTemplates = permissionService.getTemplatesForType(entityType, permType).keySet();

        List<String> permissions = permissionTemplates
                .stream()
                .map(pt -> permissionService.getPermission(pt, entityId))
                .collect(Collectors.toList());

        List<RoleEntity> roles = permissionService.finaAllRolesHavingPermissions(permissions);

        return roles.stream().map(re -> conversionService.convert(re, RoleDTO.class)).collect(Collectors.toList());
    }

    /**
     * Get roles that have a permission type (READ/WRITE) to entity
     *
     * @summary Get roles that have a specific permission (READ/WRITE) for the
     * entity
     * @param entityType The entity type
     * @param entityId The entity ID
     * @return The list of permissions for the permission type
     * @throws Exception
     */
    @GET
    @Path("/access/{entityType}/{entityId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<RoleDTO> listAccessesForEntity(
            @PathParam("entityType") EntityType entityType,
            @PathParam("entityId") Integer entityId
    ) throws Exception {
        return listAccessesForEntityByPermType(entityType, entityId, AccessType.WRITE);
    }

    /**
     * Grant group of permissions (READ / WRITE / ...) for the specified entity to the given role.
     * Only owner of the entity can do that.
     * 
     * @summary Grant permissions
     * @param entityType The entity type
     * @param entityId The entity ID
     * @param roleId The role ID
     * @param accessRequestDTO The access request object
     * @throws Exception 
     */
    @POST
    @Path("/access/{entityType}/{entityId}/role/{roleId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void grantEntityPermissionsForRole(
            @PathParam("entityType") EntityType entityType,
            @PathParam("entityId") Integer entityId,
            @PathParam("roleId") Long roleId,
            AccessRequestDTO accessRequestDTO
    ) throws Exception {

        permissionService.checkCommonEntityOwnership(entityType, entityId);

        Map<String, String> permissionTemplates = permissionService.getTemplatesForType(entityType, accessRequestDTO.getAccessType());

        RoleEntity role = permissionManager.getRole(roleId);
        permissionManager.addPermissionsFromTemplate(role, permissionTemplates, entityId.toString());
    }

    /**
     * Remove group of permissions for the specified entity to the given role.
     * 
     * @summary Remove permissions
     * @param entityType The entity type
     * @param entityId The entity ID
     * @param roleId The role ID
     * @param accessRequestDTO The access request object
     * @throws Exception 
     */
    @DELETE
    @Path("/access/{entityType}/{entityId}/role/{roleId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void revokeEntityPermissionsFromRole(
            @PathParam("entityType") EntityType entityType,
            @PathParam("entityId") Integer entityId,
            @PathParam("roleId") Long roleId,
            AccessRequestDTO accessRequestDTO
    ) throws Exception {

        permissionService.checkCommonEntityOwnership(entityType, entityId);
        Map<String, String> permissionTemplates = permissionService.getTemplatesForType(entityType, accessRequestDTO.getAccessType());
        permissionService.removePermissionsFromRole(permissionTemplates, entityId, roleId);
    }
}
