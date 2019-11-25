package org.ohdsi.webapi.security;

import org.apache.shiro.authz.UnauthorizedException;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.security.model.EntityPermissionSchema;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.security.model.EntityType;
import org.ohdsi.webapi.security.model.SourcePermissionSchema;
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
import org.springframework.aop.framework.Advised;
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

@Service
public class PermissionService {

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
            .filter(m -> m.getName().equals("getId"))
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

    private boolean isCurrentUserOwnerOf(CommonEntity entity) {

        UserEntity owner = entity.getCreatedBy();
        String loggedInUsername = permissionManager.getSubjectName();
        return Objects.equals(owner.getLogin(), loggedInUsername);
    }
}
