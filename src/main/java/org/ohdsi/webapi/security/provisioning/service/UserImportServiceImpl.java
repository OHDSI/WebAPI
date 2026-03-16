package org.ohdsi.webapi.security.provisioning.service;

import static org.ohdsi.webapi.Constants.JOB_IS_ALREADY_SCHEDULED;
import static org.ohdsi.webapi.security.provisioning.providers.AbstractLdapProvider.OBJECTCLASS_ATTR;
import static org.ohdsi.webapi.security.provisioning.providers.OhdsiLdapUtils.getCriteria;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.arachne.scheduler.model.JobExecutingType;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.Role;
import org.ohdsi.webapi.security.authz.RoleEntity;
import org.ohdsi.webapi.security.authz.UserEntity;
import org.ohdsi.webapi.security.authz.UserRepository;
import org.ohdsi.webapi.security.provisioning.JobAlreadyExistException;
import org.ohdsi.webapi.security.provisioning.RoleGroupUtils;
import org.ohdsi.webapi.security.provisioning.converter.RoleGroupMappingConverter;
import org.ohdsi.webapi.security.provisioning.model.AtlasUserRoles;
import org.ohdsi.webapi.security.provisioning.model.AuthenticationProviders;
import org.ohdsi.webapi.security.provisioning.model.ConnectionInfo;
import org.ohdsi.webapi.security.provisioning.model.LdapGroup;
import org.ohdsi.webapi.security.provisioning.model.LdapProviderType;
import org.ohdsi.webapi.security.provisioning.model.LdapUserImportStatus;
import org.ohdsi.webapi.security.provisioning.model.RoleGroupEntity;
import org.ohdsi.webapi.security.provisioning.model.RoleGroupMapping;
import org.ohdsi.webapi.security.provisioning.model.RoleGroupRepository;
import org.ohdsi.webapi.security.provisioning.model.RoleGroupsMap;
import org.ohdsi.webapi.security.provisioning.model.UserImportJob;
import org.ohdsi.webapi.security.provisioning.model.UserImportJobDTO;
import org.ohdsi.webapi.security.provisioning.model.UserImportJobRepository;
import org.ohdsi.webapi.security.provisioning.model.UserImportResult;
import org.ohdsi.webapi.security.provisioning.providers.ActiveDirectoryProvider;
import org.ohdsi.webapi.security.provisioning.providers.DefaultLdapProvider;
import org.ohdsi.webapi.security.provisioning.providers.LdapProvider;
import org.ohdsi.webapi.util.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user")
@Transactional(readOnly = true)
public class UserImportServiceImpl implements UserImportService {

  // Note: @RestController already includes @Component, so @Service is not needed

  private static final Logger logger = LoggerFactory.getLogger(UserImportService.class);

  private final Map<LdapProviderType, LdapProvider> providersMap = new HashMap<>();

  private final UserRepository userRepository;

  private final UserImportJobRepository userImportJobRepository;

  private final AuthorizationService userManager;

  private final RoleGroupRepository roleGroupMappingRepository;

  private final UserImportJobService userImportJobService;

  private final GenericConversionService conversionService;

  @Value("${security.auth.ad.default.import.group}#{T(java.util.Collections).emptyList()}")
  private List<String> defaultRoles;

  @Value("${security.auth.ad.url}")
  private String adUrl;

  @Value("${security.auth.ldap.url}")
  private String ldapUrl;

  public UserImportServiceImpl(@Autowired(required = false) ActiveDirectoryProvider activeDirectoryProvider,
                               @Autowired(required = false) DefaultLdapProvider ldapProvider,
                               UserRepository userRepository,
                               UserImportJobRepository userImportJobRepository,
                               AuthorizationService userManager,
                               RoleGroupRepository roleGroupMappingRepository,
                               @Lazy @Autowired(required = false) UserImportJobService userImportJobService,
                               GenericConversionService conversionService) {

    this.userRepository = userRepository;
    this.userImportJobRepository = userImportJobRepository;
    this.userManager = userManager;
    this.roleGroupMappingRepository = roleGroupMappingRepository;
    this.userImportJobService = userImportJobService;
    this.conversionService = conversionService;
    Optional.ofNullable(activeDirectoryProvider).ifPresent(provider -> providersMap.put(LdapProviderType.ACTIVE_DIRECTORY, provider));
    Optional.ofNullable(ldapProvider).ifPresent(provider -> providersMap.put(LdapProviderType.LDAP, provider));
  }

  protected Optional<LdapProvider> getProvider(LdapProviderType type) {

    return Optional.ofNullable(providersMap.get(type));
  }

  // ==================== REST Endpoints ====================

  /**
   * Get authentication providers
   */
  @GetMapping(
      value = "/providers",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public AuthenticationProviders getAuthenticationProviders() {
    AuthenticationProviders providers = new AuthenticationProviders();
    providers.setAdUrl(adUrl);
    providers.setLdapUrl(ldapUrl);
    return providers;
  }

  /**
   * Test connection to LDAP/AD provider
   */
  @GetMapping(
      value = "/import/{type}/test",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ConnectionInfo testConnectionEndpoint(@PathVariable("type") String type) {
    LdapProviderType provider = LdapProviderType.fromValue(type);
    ConnectionInfo result = new ConnectionInfo();
    testConnection(provider);
    result.setState(ConnectionInfo.ConnectionState.SUCCESS);
    result.setMessage("Connection success");
    return result;
  }

  /**
   * Find groups in LDAP/AD
   */
  @GetMapping(
      value = "/import/{type}/groups",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<LdapGroup> findGroupsEndpoint(
          @PathVariable("type") String type,
          @RequestParam(value = "search", required = false) String searchStr) {
    LdapProviderType provider = LdapProviderType.fromValue(type);
    return findGroups(provider, searchStr);
  }

  /**
   * Find users in directory
   */
  @PostMapping(
      value = "/import/{type}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<AtlasUserRoles> findDirectoryUsers(
          @PathVariable("type") String type,
          @RequestBody RoleGroupMapping mapping) {
    LdapProviderType provider = LdapProviderType.fromValue(type);
    return findUsers(provider, mapping);
  }

  /**
   * Import users from directory
   */
  @PostMapping(
      value = "/import",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public UserImportJobDTO importUsersEndpoint(
          @RequestBody List<AtlasUserRoles> users,
          @RequestParam(value = "provider") String provider,
          @RequestParam(value = "preserve", defaultValue = "TRUE") Boolean preserveRoles) {
    LdapProviderType providerType = LdapProviderType.fromValue(provider);

    UserImportJobDTO jobDto = new UserImportJobDTO();
    jobDto.setProviderType(providerType);
    jobDto.setPreserveRoles(preserveRoles);
    jobDto.setEnabled(true);
    jobDto.setStartDate(getJobStartDate());
    jobDto.setFrequency(JobExecutingType.ONCE);
    jobDto.setRecurringTimes(0);
    if (users != null) {
      jobDto.setUserRoles(Utils.serialize(users));
    }

    try {
      UserImportJob job = conversionService.convert(jobDto, UserImportJob.class);
      UserImportJob created = userImportJobService.createJob(job);
      return conversionService.convert(created, UserImportJobDTO.class);
    } catch (JobAlreadyExistException e) {
      throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
              String.format(JOB_IS_ALREADY_SCHEDULED, jobDto.getProviderType()));
    }
  }

  /**
   * Save role group mapping
   */
  @PostMapping(
      value = "/import/{type}/mapping",
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public void saveMappingEndpoint(@PathVariable("type") String type, @RequestBody RoleGroupMapping mapping) {
    LdapProviderType providerType = LdapProviderType.fromValue(type);
    List<RoleGroupEntity> mappingEntities = RoleGroupMappingConverter.convertRoleGroupMapping(mapping);
    saveRoleGroupMapping(providerType, mappingEntities);
  }

  /**
   * Get role group mapping
   */
  @GetMapping(
      value = "/import/{type}/mapping",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public RoleGroupMapping getMappingEndpoint(@PathVariable("type") String type) {
    LdapProviderType providerType = LdapProviderType.fromValue(type);
    List<RoleGroupEntity> mappingEntities = getRoleGroupMapping(providerType);
    return RoleGroupMappingConverter.convertRoleGroupMapping(type, mappingEntities);
  }

  private Date getJobStartDate() {
    Calendar calendar = GregorianCalendar.getInstance();
    // Job will be started in five seconds after now
    calendar.add(Calendar.SECOND, 5);
    calendar.set(Calendar.MILLISECOND, 0);

    return calendar.getTime();
  }

  // ==================== Service Methods ====================

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
      Set<String> roles = user.getRoles().stream().map(role -> role.name()).collect(Collectors.toSet());
      roles.addAll(defaultRoles);
      try {
        UserEntity userEntity = userRepository.findByLogin(login).orElseThrow();
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
      roleGroupMappingRepository.deleteAll(deleted);
    }
    if (!created.isEmpty()) {
      roleGroupMappingRepository.saveAll(created);
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

    UserEntity userEntity = userRepository.findByLogin(atlasUser.getLogin()).orElseThrow();
    return getStatus(userEntity, atlasUser.getRoles());
  }

  private LdapUserImportStatus getStatus(UserEntity userEntity,  List<Role> atlasUserRoles) {

    LdapUserImportStatus result = LdapUserImportStatus.NEW_USER;

    if (Objects.nonNull(userEntity)) {
      List<Long> atlasRoleIds = userEntity.getUserRoles().stream().map(userRole -> userRole.getRole().getId()).collect(Collectors.toList());
      List<Long> mappedRoleIds = atlasUserRoles.stream().map(role -> role.id()).collect(Collectors.toList());
      result = CollectionUtils.isEqualCollection(atlasRoleIds, mappedRoleIds) ? LdapUserImportStatus.EXISTS : LdapUserImportStatus.MODIFIED;
    }
    return result;
  }

}
