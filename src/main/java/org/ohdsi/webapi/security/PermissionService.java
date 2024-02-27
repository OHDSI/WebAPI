package org.ohdsi.webapi.security;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.security.model.EntityPermissionSchema;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.security.model.EntityType;
import org.ohdsi.webapi.security.model.SourcePermissionSchema;
import org.ohdsi.webapi.security.model.UserSimpleAuthorizationInfo;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.Subject;

@Service
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

    private final EntityGraph PERMISSION_ENTITY_GRAPH = EntityGraphUtils.fromAttributePaths("rolePermissions", "rolePermissions.role");

    public PermissionService(
            WebApplicationContext appContext,
            PermissionManager permissionManager,
            EntityPermissionSchemaResolver entityPermissionSchemaResolver,
            SourcePermissionSchema sourcePermissionSchema,
            RoleRepository roleRepository,
            SourceRepository sourceRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            ConversionService conversionService
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

    @PostConstruct
    private void postConstruct() {

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

        JpaRepository entityRepository = (JpaRepository) (((Advised) repositories.getRepositoryFor(entityType.getEntityClass())).getTargetSource().getTarget());
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

        RoleEntity role = roleRepository.findById(roleId);
        permissionTemplates.keySet()
                .forEach(pt -> {
                    String permission = getPermission(pt, entityId);
                    PermissionEntity permissionEntity = permissionRepository.findByValueIgnoreCase(permission);
                    if (permissionEntity != null) {
                        RolePermissionEntity rp = rolePermissionRepository.findByRoleAndPermission(role, permissionEntity);
                        rolePermissionRepository.delete(rp.getId());
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
}
