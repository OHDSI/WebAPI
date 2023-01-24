package org.ohdsi.webapi.user.importer.service;

import org.apache.commons.collections.CollectionUtils;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserOrigin;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.user.Role;
import org.ohdsi.webapi.user.importer.model.AtlasUserRoles;
import org.ohdsi.webapi.user.importer.model.LdapGroup;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.ohdsi.webapi.user.importer.model.LdapUserImportStatus;
import org.ohdsi.webapi.user.importer.model.RoleGroupEntity;
import org.ohdsi.webapi.user.importer.model.RoleGroupMapping;
import org.ohdsi.webapi.user.importer.model.RoleGroupsMap;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.ohdsi.webapi.user.importer.model.UserImportResult;
import org.ohdsi.webapi.user.importer.providers.ActiveDirectoryProvider;
import org.ohdsi.webapi.user.importer.providers.DefaultLdapProvider;
import org.ohdsi.webapi.user.importer.providers.LdapProvider;
import org.ohdsi.webapi.user.importer.repository.RoleGroupRepository;
import org.ohdsi.webapi.user.importer.repository.UserImportJobRepository;
import org.ohdsi.webapi.user.importer.utils.RoleGroupUtils;
import org.ohdsi.webapi.util.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.user.importer.providers.AbstractLdapProvider.OBJECTCLASS_ATTR;
import static org.ohdsi.webapi.user.importer.providers.OhdsiLdapUtils.getCriteria;

@Service
@Transactional(readOnly = true)
public class UserImportServiceImpl implements UserImportService {

  private static final Logger logger = LoggerFactory.getLogger(UserImportService.class);

  private final Map<LdapProviderType, LdapProvider> providersMap = new HashMap<>();

  private final UserRepository userRepository;

  private final UserImportJobRepository userImportJobRepository;

  private final PermissionManager userManager;

  private final RoleGroupRepository roleGroupMappingRepository;

  @Value("${security.ad.default.import.group}#{T(java.util.Collections).emptyList()}")
  private List<String> defaultRoles;

  public UserImportServiceImpl(@Autowired(required = false) ActiveDirectoryProvider activeDirectoryProvider,
                               @Autowired(required = false) DefaultLdapProvider ldapProvider,
                               UserRepository userRepository,
                               UserImportJobRepository userImportJobRepository,
                               PermissionManager userManager,
                               RoleGroupRepository roleGroupMappingRepository) {

    this.userRepository = userRepository;
    this.userImportJobRepository = userImportJobRepository;
    this.userManager = userManager;
    this.roleGroupMappingRepository = roleGroupMappingRepository;
    Optional.ofNullable(activeDirectoryProvider).ifPresent(provider -> providersMap.put(LdapProviderType.ACTIVE_DIRECTORY, provider));
    Optional.ofNullable(ldapProvider).ifPresent(provider -> providersMap.put(LdapProviderType.LDAP, provider));
  }

  protected Optional<LdapProvider> getProvider(LdapProviderType type) {

    return Optional.ofNullable(providersMap.get(type));
  }

  @Override
  public List<LdapGroup> findGroups(LdapProviderType type, String searchStr) {

    LdapProvider provider = getProvider(type).orElseThrow(IllegalArgumentException::new);
    return provider.findGroups(searchStr);
  }

  @Override
  public List<AtlasUserRoles> findUsers(LdapProviderType providerType, RoleGroupMapping mapping) {

    LdapProvider provider = getProvider(providerType).orElseThrow(IllegalArgumentException::new);

    return provider.findUsers().stream()
            .map(user -> {
              AtlasUserRoles atlasUser = new AtlasUserRoles();
              atlasUser.setDisplayName(user.getDisplayName());
              atlasUser.setLogin(UserUtils.toLowerCase(user.getLogin()));
              List<Role> roles = user.getGroups().stream()
                      .flatMap(g -> mapping.getRoleGroups()
                              .stream()
                              .filter(m -> m.getGroups().stream().anyMatch(group -> Objects.equals(g.getDistinguishedName(), group.getDistinguishedName())))
                              .map(RoleGroupsMap::getRole))
                      .distinct()
                      .collect(Collectors.toList());
              atlasUser.setRoles(roles);
              atlasUser.setStatus(getStatus(atlasUser));
              return atlasUser;
            })
            .filter(user -> !LdapUserImportStatus.EXISTS.equals(user.getStatus()))
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public UserImportResult importUsers(List<AtlasUserRoles> users, LdapProviderType providerType, boolean preserveRoles) {

    UserImportResult result = new UserImportResult();
    UserOrigin userOrigin = UserOrigin.getFrom(providerType);
    users.forEach(user -> {
      String login = UserUtils.toLowerCase(user.getLogin());
      Set<String> roles = user.getRoles().stream().map(role -> role.role).collect(Collectors.toSet());
      roles.addAll(defaultRoles);
      try {
        UserEntity userEntity = userRepository.findByLogin(login);
        if (Objects.nonNull(userEntity)) {
          userEntity.setName(user.getDisplayName());
          userEntity.setOrigin(userOrigin);
          if (LdapUserImportStatus.MODIFIED.equals(getStatus(userEntity, user.getRoles()))) {
            Set<RoleEntity> userRoles = userManager.getUserRoles(userEntity.getId());
            if (!preserveRoles) {
              //Overrides assigned roles
              userRoles.stream().filter(role -> !role.getName().equalsIgnoreCase(login)).forEach(r -> {
                try {
                  userManager.removeUserFromRole(r.getName(), userEntity.getLogin(), null);
                } catch (Exception e) {
                  logger.warn("Failed to remove user {} from role {}", userEntity.getLogin(), r.getName(), e);
                }
              });
            } else {
              //Filter roles that is already assigned
              roles = roles.stream()
                      .filter(role -> userRoles.stream().noneMatch(ur -> Objects.equals(ur.getName(), role)))
                      .collect(Collectors.toSet());
            }
            roles.forEach(r -> {
              try {
                userManager.addUserToRole(r, userEntity.getLogin(), userOrigin);
              } catch (Exception e) {
                logger.error("Failed to add user {} to role {}", userEntity.getLogin(), r, e);
              }
            });
            result.incUpdated();
          }
        } else {
          userManager.registerUser(login, user.getDisplayName(), userOrigin, roles);
          result.incCreated();
        }
      } catch (Exception e) {
        logger.error("Failed to register user {}", login, e);
      }
    });
    userRepository.findByOrigin(userOrigin).stream()
            .filter(existingUser -> users.stream()
                    .noneMatch(user -> UserUtils.toLowerCase(user.getLogin()).equals(existingUser.getLogin())))
            .forEach(deletedUser -> deletedUser.getUserRoles().stream()
                    .filter(role -> !role.getRole().getName().equalsIgnoreCase(deletedUser.getLogin()))
                    .forEach(role -> userManager.removeUserFromRole(role.getRole().getName(), deletedUser.getLogin(), userOrigin)));
    return result;
  }

  @Override
  @Transactional
  public void saveRoleGroupMapping(LdapProviderType providerType, List<RoleGroupEntity> mappingEntities) {

    List<RoleGroupEntity> exists = roleGroupMappingRepository.findByProviderAndUserImportJobNull(providerType);
    List<RoleGroupEntity> deleted = RoleGroupUtils.findDeleted(exists, mappingEntities);
    List<RoleGroupEntity> created = RoleGroupUtils.findCreated(exists, mappingEntities);
    if (!deleted.isEmpty()) {
      roleGroupMappingRepository.delete(deleted);
    }
    if (!created.isEmpty()) {
      roleGroupMappingRepository.save(created);
    }
  }

  @Override
  public List<RoleGroupEntity> getRoleGroupMapping(LdapProviderType providerType) {

    return roleGroupMappingRepository.findByProviderAndUserImportJobNull(providerType);
  }

  @Override
  public void testConnection(LdapProviderType providerType) {

    LdapProvider provider = getProvider(providerType).orElseThrow(IllegalArgumentException::new);
    LdapTemplate ldapTemplate = provider.getLdapTemplate();
    AndFilter filter = new AndFilter();
    filter.and(getCriteria(OBJECTCLASS_ATTR, getProvider(providerType).orElseThrow(IllegalArgumentException::new).getGroupClasses()))
            .and(new EqualsFilter(provider.getLoginAttributeName(), provider.getPrincipal()));
    ldapTemplate.authenticate(LdapUtils.emptyLdapName(), filter.toString(), provider.getPassword());
  }

    @Override
    public UserImportJob getImportUserJob(Long userImportId) {
      return userImportJobRepository.getOne(userImportId);
    }

    private LdapUserImportStatus getStatus(AtlasUserRoles atlasUser) {

    UserEntity userEntity = userRepository.findByLogin(atlasUser.getLogin());
    return getStatus(userEntity, atlasUser.getRoles());
  }

  private LdapUserImportStatus getStatus(UserEntity userEntity,  List<Role> atlasUserRoles) {

    LdapUserImportStatus result = LdapUserImportStatus.NEW_USER;

    if (Objects.nonNull(userEntity)) {
      List<Long> atlasRoleIds = userEntity.getUserRoles().stream().map(userRole -> userRole.getRole().getId()).collect(Collectors.toList());
      List<Long> mappedRoleIds = atlasUserRoles.stream().map(role -> role.id).collect(Collectors.toList());
      result = CollectionUtils.isEqualCollection(atlasRoleIds, mappedRoleIds) ? LdapUserImportStatus.EXISTS : LdapUserImportStatus.MODIFIED;
    }
    return result;
  }

}
