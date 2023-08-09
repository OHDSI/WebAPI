package org.ohdsi.webapi.security;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.security.dto.RoleDTO;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private ThreadLocal<ConcurrentHashMap<EntityType, ConcurrentHashMap<String, Set<RoleDTO>>>> permissionCache =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

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


    public void preparePermissionCache(EntityType entityType, Set<String> permissionTemplates) {
        if (permissionCache.get().get(entityType) == null) {
            final ConcurrentHashMap<String, Set<RoleDTO>> rolesForEntity = new ConcurrentHashMap<>();
            permissionCache.get().put(entityType, rolesForEntity);

            List<String> permissionsSQLTemplates = permissionTemplates.stream()
                    .map(pt -> getPermissionSqlTemplate(pt))
                    .collect(Collectors.toList());

            Map<Long, RoleDTO> roleDTOMap = new HashMap<>();
            permissionsSQLTemplates.forEach(p -> {
                Iterable<PermissionEntity> permissionEntities = permissionRepository.findByValueLike(p, PERMISSION_ENTITY_GRAPH);
                for (PermissionEntity permissionEntity : permissionEntities) {
                    Set<RoleDTO> roles = rolesForEntity.get(permissionEntity.getValue());
                    if (roles == null) {
                        rolesForEntity.put(permissionEntity.getValue(), new HashSet<>());
                    }
                    Set<RoleDTO> cachedRoles = rolesForEntity.get(permissionEntity.getValue());
                    permissionEntity.getRolePermissions().forEach(rp -> {
                        RoleDTO roleDTO = roleDTOMap.get(rp.getRole().getId());
                        if (roleDTO == null) {
                            roleDTO = conversionService.convert(rp.getRole(), RoleDTO.class);
                            roleDTOMap.put(roleDTO.getId(), roleDTO);
                        }
                        cachedRoles.add(roleDTO);
                    });
                }
            });
        }
    }

    public List<RoleDTO> getRolesHavingPermissions(EntityType entityType, Number id) {
        Set<String> permissionTemplates = getTemplatesForType(entityType, AccessType.WRITE).keySet();
        preparePermissionCache(entityType, permissionTemplates);

        List<String> permissions = permissionTemplates.stream()
                .map(pt -> getPermission(pt, id))
                .collect(Collectors.toList());
        int fitCount = permissions.size();
        Map<RoleDTO, Long> roleMap = permissions.stream()
                .filter(p -> permissionCache.get().get(entityType).get(p) != null)
                .flatMap(p -> permissionCache.get().get(entityType).get(p).stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<RoleDTO> roles = roleMap.entrySet().stream()
                .filter(es -> es.getValue() == fitCount)
                .map(es -> es.getKey())
                .collect(Collectors.toList());
        return roles;
    }

    public List<RoleDTO> getRolesHavingReadPermissions(EntityType entityType, Number id) {
        Set<String> permissionTemplates = getTemplatesForType(entityType, AccessType.READ).keySet();
        preparePermissionCache(entityType, permissionTemplates);

        List<String> permissions = permissionTemplates.stream()
                .map(pt -> getPermission(pt, id))
                .collect(Collectors.toList());
        int fitCount = permissions.size();
        Map<RoleDTO, Long> roleMap = permissions.stream()
                .filter(p -> permissionCache.get().get(entityType).get(p) != null)
                .flatMap(p -> permissionCache.get().get(entityType).get(p).stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<RoleDTO> roles = roleMap.entrySet().stream()
                .filter(es -> es.getValue() == fitCount)
                .map(es -> es.getKey())
                .collect(Collectors.toList());
        return roles;
    }

    public void clearPermissionCache() {
        this.permissionCache.set(new ConcurrentHashMap<>());
    }

    public boolean hasWriteAccess(CommonEntity entity) {
        boolean hasAccess = false;
        if (securityEnabled && entity.getCreatedBy() != null) {
            try {
                String login = this.permissionManager.getSubjectName();
                UserSimpleAuthorizationInfo authorizationInfo = this.permissionManager.getAuthorizationInfo(login);
                if (Objects.equals(authorizationInfo.getUserId(), entity.getCreatedBy().getId())) {
                    hasAccess = true; // the role is the one that created the artifact
                } else {
                    EntityType entityType = entityPermissionSchemaResolver.getEntityType(entity.getClass());

                    List<RoleDTO> roles = getRolesHavingPermissions(entityType, entity.getId());

                    Collection<String> userRoles = authorizationInfo.getRoles();
                    hasAccess = roles.stream()
                            .anyMatch(r -> userRoles.stream()
                            .anyMatch(re -> re.equals(r.getName())));
                }
            } catch (Exception e) {
                logger.error("Error getting user roles and permissions", e);
                throw new RuntimeException(e);
            }
        }
        return hasAccess;
    }


    public boolean hasReadAccess(CommonEntity entity) {
        boolean hasAccess = false;
        if (securityEnabled && entity.getCreatedBy() != null) {
            try {
                String login = this.permissionManager.getSubjectName();
                UserSimpleAuthorizationInfo authorizationInfo = this.permissionManager.getAuthorizationInfo(login);
                if (Objects.equals(authorizationInfo.getUserId(), entity.getCreatedBy().getId())){
		    hasAccess = true; // the role is the one that created the artifact
		} else {
                    EntityType entityType = entityPermissionSchemaResolver.getEntityType(entity.getClass());

                    List<RoleDTO> roles = getRolesHavingReadPermissions(entityType, entity.getId());

                    Collection<String> userRoles = authorizationInfo.getRoles();
                    hasAccess = roles.stream()
                            .anyMatch(r -> userRoles.stream()
                                    .anyMatch(re -> re.equals(r.getName())));
                }
            } catch (Exception e) {
                logger.error("Error getting user roles and permissions", e);
                throw new RuntimeException(e);
            }
        }
        return hasAccess;
    }

    public void fillWriteAccess(CommonEntity entity, CommonEntityDTO entityDTO) {
        if (securityEnabled && entity.getCreatedBy() != null) {
            entityDTO.setHasWriteAccess(hasWriteAccess(entity));
        }
    }

    public void fillReadAccess(CommonEntity entity, CommonEntityDTO entityDTO) {
        if (securityEnabled && entity.getCreatedBy() != null) {
            entityDTO.setHasReadAccess(hasReadAccess(entity));
        }
    }
    
    public boolean isSecurityEnabled() {
      return this.securityEnabled;
    }
}
