package org.ohdsi.webapi.security;

import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain2.DynamicEntityGraph;
import org.apache.shiro.authz.UnauthorizedException;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.security.dto.AccessRequestDTO;
import org.ohdsi.webapi.security.dto.RoleDTO;
import org.ohdsi.webapi.security.model.EntityPermissionSchema;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.security.model.EntityType;
import org.ohdsi.webapi.security.model.SourcePermissionSchema;
import org.ohdsi.webapi.security.model.UserSimpleAuthorizationInfo;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.PermissionRepository;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.RolePermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RolePermissionRepository;
import org.ohdsi.webapi.shiro.Entities.RoleRepository;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import java.io.Serializable;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.Subject;

@RestController
@RequestMapping("/permission")
@Transactional
public class PermissionService {
    private final Logger logger = LoggerFactory.getLogger(PermissionService.class);

    private final WebApplicationContext appContext;
    private final PermissionManager permissionManager;
    private final EntityPermissionSchemaResolver entityPermissionSchemaResolver;
    private final SourcePermissionSchema sourcePermissionSchema;
    private final RoleRepository roleRepository;
    private final SourceRepository sourceRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final ConversionService conversionService;

    private Repositories repositories;

    @Value("#{!'${security.provider}'.equals('DisabledSecurity')}")
    private boolean securityEnabled;

	@Value("${security.defaultGlobalReadPermissions}")
	private boolean defaultGlobalReadPermissions;

    private final EntityGraph PERMISSION_ENTITY_GRAPH = DynamicEntityGraph.loading().addPath("rolePermissions", "rolePermissions.role").build();

    public PermissionService(
            WebApplicationContext appContext,
            PermissionManager permissionManager,
            EntityPermissionSchemaResolver entityPermissionSchemaResolver,
            SourcePermissionSchema sourcePermissionSchema,
            RoleRepository roleRepository,
            SourceRepository sourceRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            @Qualifier("conversionService") ConversionService conversionService
    ) {

        this.appContext = appContext;
        this.permissionManager = permissionManager;
        this.entityPermissionSchemaResolver = entityPermissionSchemaResolver;
        this.sourcePermissionSchema = sourcePermissionSchema;
        this.roleRepository = roleRepository;
        this.sourceRepository = sourceRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.conversionService = conversionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void postConstruct() {

        this.repositories = new Repositories(appContext);

        // Migrated from Atlas security. Is it still required?
        for (Source source : sourceRepository.findAll()) {
            sourcePermissionSchema.addSourceUserRole(source);
        }
    }

    public List<RoleEntity> suggestRoles(String roleSearch) {

        return roleRepository.findByNameIgnoreCaseContaining(roleSearch);
    }

    public Map<String, String> getTemplatesForType(EntityType entityType, AccessType accessType) {

        EntityPermissionSchema entityPermissionSchema = entityPermissionSchemaResolver.getForType(entityType);
        return getPermissionTemplates(entityPermissionSchema, accessType);
    }

    public void checkCommonEntityOwnership(EntityType entityType, Integer entityId) throws Exception {

        Object repositoryTarget = repositories.getRepositoryFor(entityType.getEntityClass());
        if (!(repositoryTarget instanceof Advised)) {
            throw new IllegalStateException("Repository is not advised");
        }
        JpaRepository entityRepository = (JpaRepository) (((Advised) repositoryTarget).getTargetSource().getTarget());
        Class idClazz = Arrays.stream(entityType.getEntityClass().getMethods())
            // Overriden methods from parameterized interface are "bridges" and should be ignored.
            // For more information see https://docs.oracle.com/javase/tutorial/java/generics/bridgeMethods.html
            .filter(m -> m.getName().equals("getId") && !m.isBridge())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Cannot retrieve common entity"))
            .getReturnType();
        CommonEntity entity = (CommonEntity) entityRepository.getOne((Serializable) conversionService.convert(entityId, idClazz));

        if (!isCurrentUserOwnerOf(entity)) {
            throw new UnauthorizedException();
        }
    }

    public Map<String, String> getPermissionTemplates(EntityPermissionSchema permissionSchema, AccessType accessType) {

      switch (accessType) {
        case WRITE:
          return permissionSchema.getWritePermissions();
        case READ:
          return permissionSchema.getReadPermissions();
        default:
          throw new UnsupportedOperationException();
      }
    }

    public List<RoleEntity> finaAllRolesHavingPermissions(List<String> permissions) {

        return roleRepository.finaAllRolesHavingPermissions(permissions, (long) permissions.size());
    }

    public void removePermissionsFromRole(Map<String, String> permissionTemplates, Integer entityId, Long roleId) {

        RoleEntity role = roleRepository.findById(roleId).orElse(null);
        permissionTemplates.keySet()
                .forEach(pt -> {
                    String permission = getPermission(pt, entityId);
                    PermissionEntity permissionEntity = permissionRepository.findByValueIgnoreCase(permission);
                    if (permissionEntity != null) {
                        RolePermissionEntity rp = rolePermissionRepository.findByRoleAndPermission(role, permissionEntity);
                        rolePermissionRepository.deleteById(rp.getId());
                    }
                });
    }

    public String getPermission(String template, Object entityId) {

        return String.format(template, entityId);
    }

    public String getPermissionSqlTemplate(String template) {
        return String.format(template, "%%");
    }

    private boolean isCurrentUserOwnerOf(CommonEntity entity) {

        UserEntity owner = entity.getCreatedBy();
        String loggedInUsername = permissionManager.getSubjectName();
        return Objects.equals(owner.getLogin(), loggedInUsername);
    }

    public List<Permission> getEntityPermissions(EntityType entityType, Number id, AccessType accessType) {
        Set<String> permissionTemplates = getTemplatesForType(entityType, accessType).keySet();

        List<Permission> permissions = permissionTemplates.stream()
                .map(pt -> new WildcardPermission(getPermission(pt, id)))
                .collect(Collectors.toList());
        return permissions;
    }

    public boolean hasAccess(CommonEntity entity, AccessType accessType) {
        boolean hasAccess = false;
        if (securityEnabled && entity.getCreatedBy() != null) {
            try {
                Subject subject = SecurityUtils.getSubject();
                String login = this.permissionManager.getSubjectName();
                UserSimpleAuthorizationInfo authorizationInfo = this.permissionManager.getAuthorizationInfo(login);
                if (Objects.equals(authorizationInfo.getUserId(), entity.getCreatedBy().getId())) {
                    hasAccess = true; // the role is the one that created the artifact
                } else {
                    EntityType entityType = entityPermissionSchemaResolver.getEntityType(entity.getClass());
                    List<Permission> permsToCheck = getEntityPermissions(entityType, entity.getId(), accessType);
                    hasAccess = permsToCheck.stream().allMatch(p -> subject.isPermitted(p));
                }
            } catch (Exception e) {
                logger.error("Error getting user roles and permissions", e);
                throw new RuntimeException(e);
            }
        }
        return hasAccess;
    }
    
    public boolean hasWriteAccess(CommonEntity entity) {
      return hasAccess(entity, AccessType.WRITE);
    }

    public boolean hasReadAccess(CommonEntity entity) {
      return hasAccess(entity, AccessType.READ);
    }

    public void fillWriteAccess(CommonEntity entity, CommonEntityDTO entityDTO) {
        if (securityEnabled && entity.getCreatedBy() != null) {
            entityDTO.setHasWriteAccess(hasAccess(entity, AccessType.WRITE));
        }
    }

    public void fillReadAccess(CommonEntity entity, CommonEntityDTO entityDTO) {
        if (securityEnabled && entity.getCreatedBy() != null) {
            entityDTO.setHasReadAccess(hasAccess(entity, AccessType.READ));
        }
    }
    
    public boolean isSecurityEnabled() {
      return this.securityEnabled;
    }

		// Use this key for cache (asset lists) that may be associated to a user or shared across users.
		public String getAssetListCacheKey() {
			if (this.isSecurityEnabled() && !defaultGlobalReadPermissions) 
				return permissionManager.getSubjectName();
			else
				return "ALL_USERS";
		}
		
		// use this cache key when the cache is associated to a user
		public String getSubjectCacheKey() {
			return this.isSecurityEnabled() ? permissionManager.getSubjectName() : "ALL_USERS";
		}

    // ==================== REST Endpoints ====================

    /**
     * Get the list of permissions for a user
     *
     * @return A list of permissions
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserService.Permission> getPermissions() {
        Iterable<PermissionEntity> permissionEntities = permissionManager.getPermissions();
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
    @GetMapping(
        value = "/access/suggest",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<RoleDTO> listAccessesForEntitySuggest(@RequestParam("roleSearch") String roleSearch) {
        List<RoleEntity> roles = suggestRoles(roleSearch);
        return roles.stream()
            .map(re -> conversionService.convert(re, RoleDTO.class))
            .collect(Collectors.toList());
    }

    /**
     * Get roles that have a permission type (READ/WRITE) to entity
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
    public List<RoleDTO> listAccessesForEntityByPermType(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Integer entityId,
            @PathVariable("permType") AccessType permType) throws Exception {
        checkCommonEntityOwnership(entityType, entityId);
        var permissionTemplates = getTemplatesForType(entityType, permType).keySet();

        List<String> permissions = permissionTemplates.stream()
                .map(pt -> getPermission(pt, entityId))
                .collect(Collectors.toList());

        List<RoleEntity> roles = finaAllRolesHavingPermissions(permissions);

        return roles.stream()
            .map(re -> conversionService.convert(re, RoleDTO.class))
            .collect(Collectors.toList());
    }

    /**
     * Get roles that have a permission type (READ/WRITE) to entity
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
    public List<RoleDTO> listAccessesForEntity(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Integer entityId) throws Exception {
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
    @PostMapping(
        value = "/access/{entityType}/{entityId}/role/{roleId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void grantEntityPermissionsForRole(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Integer entityId,
            @PathVariable("roleId") Long roleId,
            @RequestBody AccessRequestDTO accessRequestDTO) throws Exception {
        checkCommonEntityOwnership(entityType, entityId);

        var permissionTemplates = getTemplatesForType(entityType, accessRequestDTO.getAccessType());

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
    @DeleteMapping(
        value = "/access/{entityType}/{entityId}/role/{roleId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void revokeEntityPermissionsFromRole(
            @PathVariable("entityType") EntityType entityType,
            @PathVariable("entityId") Integer entityId,
            @PathVariable("roleId") Long roleId,
            @RequestBody AccessRequestDTO accessRequestDTO) throws Exception {
        checkCommonEntityOwnership(entityType, entityId);
        var permissionTemplates = getTemplatesForType(entityType, accessRequestDTO.getAccessType());
        removePermissionsFromRole(permissionTemplates, entityId, roleId);
    }
}
