package org.ohdsi.webapi.user.importer;

import org.apache.commons.collections.CollectionUtils;
import org.ohdsi.webapi.user.importer.model.*;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.user.importer.providers.ActiveDirectoryProvider;
import org.ohdsi.webapi.user.importer.providers.DefaultLdapProvider;
import org.ohdsi.webapi.user.importer.providers.LdapProvider;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.*;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.WhitespaceWildcardsFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.directory.DirContext;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.user.importer.providers.OhdsiLdapUtils.getCriteria;
import static org.ohdsi.webapi.user.importer.providers.OhdsiLdapUtils.valueAsString;

@Component
@Transactional(readOnly = true)
public class DefaultUserImporter implements UserImporter {

  private static final Logger logger = LoggerFactory.getLogger(UserImporter.class);
  private static final String OBJECTCLASS_ATTR = "objectclass";
  private static final String CN_ATTR = "cn";

  private final Map<LdapProviderType, LdapProvider> providersMap = new HashMap<>();

  private final UserRepository userRepository;

  private final PermissionManager userManager;

  private final RoleGroupMappingRepository roleGroupMappingRepository;

  public DefaultUserImporter(@Autowired(required = false) ActiveDirectoryProvider activeDirectoryProvider,
                             @Autowired(required = false) DefaultLdapProvider ldapProvider,
                             UserRepository userRepository,
                             PermissionManager userManager,
                             RoleGroupMappingRepository roleGroupMappingRepository) {

    this.userRepository = userRepository;
    this.userManager = userManager;
    this.roleGroupMappingRepository = roleGroupMappingRepository;

    Optional.ofNullable(activeDirectoryProvider).ifPresent(provider -> providersMap.put(LdapProviderType.ACTIVE_DIRECTORY, provider));
    Optional.ofNullable(ldapProvider).ifPresent(provider -> providersMap.put(LdapProviderType.LDAP, provider));
  }

  protected Optional<LdapProvider> getProvider(LdapProviderType type) {

    return Optional.ofNullable(providersMap.get(type));
  }

  @Override
  public LdapTemplate getLdapTemplate(LdapProviderType providerType) {

    return getProvider(providerType).orElseThrow(IllegalArgumentException::new).getLdapTemplate();
  }

  @Override
  public List<LdapGroup> findGroups(LdapProviderType type, String searchStr) {

    LdapProvider provider = getProvider(type).orElseThrow(IllegalArgumentException::new);
    LdapTemplate ldapTemplate = getLdapTemplate(type);
    AndFilter filter = new AndFilter();
    filter.and(getCriteria(OBJECTCLASS_ATTR, getProvider(type).orElseThrow(IllegalArgumentException::new).getGroupClasses()));
    filter.and(new WhitespaceWildcardsFilter(CN_ATTR, searchStr));
    return ldapTemplate.search(LdapUtils.emptyLdapName(), filter.encode(), getAttributesMapper(provider, LdapGroup::new));
  }

  @Override
  public List<AtlasUserRoles> findUsers(LdapProviderType providerType, RoleGroupMapping mapping) {

    LdapTemplate ldapTemplate = getLdapTemplate(providerType);
    LdapProvider provider = getProvider(providerType).orElseThrow(IllegalArgumentException::new);
    AndFilter filter = new AndFilter();
    filter.and(getCriteria(OBJECTCLASS_ATTR, getProvider(providerType).orElseThrow(IllegalArgumentException::new).getUserClass()));
    CollectingNameClassPairCallbackHandler<LdapUser> handler =  provider.getUserSearchCallbackHandler(getUserAttributesMapper(providerType));
    ldapTemplate.search(LdapUtils.emptyLdapName(), filter.encode(), provider.getUserSearchControls(), handler, new LdapTemplate.NullDirContextProcessor());
    List<LdapUser> users = handler.getList();
    return users.stream()
            .map(user -> {
              AtlasUserRoles atlasUser = new AtlasUserRoles();
              atlasUser.setDisplayName(user.getDisplayName());
              atlasUser.setLogin(user.getLogin());
              List<UserService.Role> roles = user.getGroups().stream()
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
  public void importUsers(List<AtlasUserRoles> users) {

    users.forEach(user -> {
      Set<String> roles = user.getRoles().stream().map(role -> role.role).collect(Collectors.toSet());
      try {
        UserEntity userEntity;
        if (LdapUserImportStatus.MODIFIED.equals(user.getStatus()) && Objects.nonNull(userEntity = userRepository.findByLogin(user.getLogin()))) {
          Set<RoleEntity> userRoles = userManager.getUserRoles(userEntity.getId());
          userRoles.forEach(r -> {
            try {
              userManager.removeUserFromRole(r.getName(), userEntity.getLogin());
            } catch (Exception e) {
              logger.warn("Failed to remove user {} from role {}", userEntity.getLogin(), r.getName(), e);
            }
          });
          roles.forEach(r -> {
            try {
              userManager.addUserToRole(r, userEntity.getLogin());
            } catch (Exception e) {
              logger.error("Failed to add user {} to role {}", userEntity.getLogin(), r, e);
            }
          });
        } else {
          userManager.registerUser(user.getLogin(), roles);
        }
      } catch (Exception e) {
        logger.error("Failed to register user {}", user.getLogin(), e);
      }
    });
  }

  @Override
  @Transactional
  public void saveRoleGroupMapping(LdapProviderType providerType, List<RoleGroupMappingEntity> mappingEntities) {

    List<RoleGroupMappingEntity> exists = roleGroupMappingRepository.findByProvider(providerType);
    List<RoleGroupMappingEntity> deleted = exists
            .stream()
            .filter(e -> mappingEntities.stream().noneMatch(m -> equalsRoleGroupMapping(e, m)))
            .collect(Collectors.toList());
    List<RoleGroupMappingEntity> created = mappingEntities
            .stream()
            .filter(m -> exists.stream().noneMatch(e -> equalsRoleGroupMapping(e, m)))
            .collect(Collectors.toList());
    if (!deleted.isEmpty()) {
      roleGroupMappingRepository.delete(deleted);
    }
    if (!created.isEmpty()) {
      roleGroupMappingRepository.save(created);
    }
  }

  private boolean equalsRoleGroupMapping(RoleGroupMappingEntity a, RoleGroupMappingEntity b) {
    if (Objects.isNull(a) && Objects.isNull(b)) {
      return true;
    }
    if (Objects.nonNull(a) && Objects.nonNull(b)) {
      return Objects.equals(a.getProvider(), b.getProvider())
              && Objects.equals(a.getGroupDn(), b.getGroupDn())
              && Objects.equals(a.getRole().getId(), b.getRole().getId());
    }
    return false;
  }


  @Override
  public List<RoleGroupMappingEntity> getRoleGroupMapping(LdapProviderType providerType) {

    return roleGroupMappingRepository.findByProvider(providerType);
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

  private LdapUserImportStatus getStatus(AtlasUserRoles atlasUser) {

    LdapUserImportStatus result = LdapUserImportStatus.NEW_USER;
    UserEntity userEntity = userRepository.findByLogin(atlasUser.getLogin());
    if (Objects.nonNull(userEntity)) {
      List<Long> atlasRoleIds = userEntity.getUserRoles().stream().map(userRole -> userRole.getRole().getId()).collect(Collectors.toList());
      List<Long> mappedRoleIds = atlasUser.getRoles().stream().map(role -> role.id).collect(Collectors.toList());
      result = CollectionUtils.isEqualCollection(atlasRoleIds, mappedRoleIds) ? LdapUserImportStatus.EXISTS : LdapUserImportStatus.MODIFIED;
    }
    return result;
  }

  private <T extends LdapObject> AttributesMapper<T> getAttributesMapper(LdapProvider provider, Supplier<T> supplier) {
    return attributes -> {
      String name = valueAsString(attributes.get(provider.getDisplayNameAttributeName()));
      String dn = valueAsString(attributes.get(provider.getDistinguishedAttributeName()));
      T object = supplier.get();
      object.setDisplayName(name);
      object.setDistinguishedName(dn);
      return object;
    };
  }

  private AttributesMapper<LdapUser> getUserAttributesMapper(LdapProviderType providerType) {

    LdapProvider provider = getProvider(providerType).orElseThrow(IllegalArgumentException::new);
    return attributes -> {
      LdapUser user = getAttributesMapper(provider, LdapUser::new).mapFromAttributes(attributes);
      user.setLogin(valueAsString(attributes.get(provider.getLoginAttributeName())));
      List<LdapGroup> groups = provider.getLdapGroups(attributes);
      user.setGroups(groups);
      return user;
    };
  }

}
