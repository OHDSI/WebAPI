package org.ohdsi.webapi.user.importer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.user.importer.RoleGroupMappingEntity;
import org.ohdsi.webapi.user.importer.model.*;
import org.ohdsi.webapi.user.importer.UserImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Path("/")
public class UserImportService {

  private static final Logger logger = LoggerFactory.getLogger(UserImportService.class);

  @Autowired
  private UserImporter userImporter;

  @Value("${security.ad.url}")
  private String adUrl;

  @Value("${security.ldap.url}")
  private String ldapUrl;

  @Value("${security.ad.default.import.group}#{T(java.util.Collections).emptyList()}")
  private List<String> defaultRoles;

  @GET
  @Path("user/providers")
  @Produces(MediaType.APPLICATION_JSON)
  public AuthenticationProviders getAuthenticationProviders() {
    AuthenticationProviders providers = new AuthenticationProviders();
    providers.setAdUrl(adUrl);
    providers.setLdapUrl(ldapUrl);
    return providers;
  }

  @GET
  @Path("user/import/{type}/test")
  @Produces(MediaType.APPLICATION_JSON)
  public Response testConnection(@PathParam("type") String type) {
    LdapProviderType provider = LdapProviderType.fromValue(type);
    ConnectionInfo result = new ConnectionInfo();
    try {
      userImporter.testConnection(provider);
      result.setState(ConnectionInfo.ConnectionState.SUCCESS);
      result.setMessage("Connection success");
    } catch(Exception e) {
      logger.error("LDAP connection failed.", e);
      result.setMessage("Connection failed. " + e.getMessage());
      StringWriter out = new StringWriter();
      try(PrintWriter writer = new PrintWriter(out)) {
        e.printStackTrace(writer);
        result.setDetails(out.toString());
      }
      result.setState(ConnectionInfo.ConnectionState.FAILED);
    }
    return Response.ok().entity(result).build();
  }

  @GET
  @Path("user/import/{type}/groups")
  @Produces(MediaType.APPLICATION_JSON)
  public List<LdapGroup> findGroups(@PathParam("type") String type, @QueryParam("search") String searchStr) {
    LdapProviderType provider = LdapProviderType.fromValue(type);
    return userImporter.findGroups(provider, searchStr);
  }

  @POST
  @Path("user/import/{type}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<AtlasUserRoles> findDirectoryUsers(@PathParam("type") String type, RoleGroupMapping mapping){
    LdapProviderType provider = LdapProviderType.fromValue(type);
    return userImporter.findUsers(provider, mapping);
  }

  @POST
  @Path("user/import")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response importUsers(List<AtlasUserRoles> users) {
    userImporter.importUsers(users, defaultRoles);
    return Response.ok().build();
  }

  @POST
  @Path("user/import/{type}/mapping")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response saveMapping(@PathParam("type") String type, RoleGroupMapping mapping) {
    LdapProviderType providerType = LdapProviderType.fromValue(type);
    List<RoleGroupMappingEntity> mappingEntities = convertRoleGroupMapping(mapping);
    userImporter.saveRoleGroupMapping(providerType, mappingEntities);
    return Response.ok().build();
  }

  @GET
  @Path("user/import/{type}/mapping")
  @Produces(MediaType.APPLICATION_JSON)
  public RoleGroupMapping getMapping(@PathParam("type") String type) {
    LdapProviderType providerType = LdapProviderType.fromValue(type);
    List<RoleGroupMappingEntity> mappingEntities = userImporter.getRoleGroupMapping(providerType);
    return convertRoleGroupMapping(type, mappingEntities);
  }

  private RoleGroupMapping convertRoleGroupMapping(String provider, List<RoleGroupMappingEntity> mappingEntities) {

    RoleGroupMapping roleGroupMapping = new RoleGroupMapping();
    roleGroupMapping.setProvider(provider);
    Map<Long, List<RoleGroupMappingEntity>> entityMap = mappingEntities.stream()
            .collect(Collectors.groupingBy(r -> r.getRole().getId()));
    Map<Long, RoleEntity> roleMap = entityMap.entrySet().stream().map(e -> new ImmutablePair<>(e.getKey(), e.getValue().iterator().next().getRole()))
            .collect(Collectors.toMap(ImmutablePair::getKey, ImmutablePair::getValue));

    List<RoleGroupsMap> roleGroups = entityMap
            .entrySet().stream().map(entry -> {
              RoleGroupsMap roleGroupsMap = new RoleGroupsMap();
              roleGroupsMap.setRole(new UserService.Role(roleMap.get(entry.getKey())));
              List<LdapGroup> groups = entry
                      .getValue()
                      .stream()
                      .map(role -> new LdapGroup(role.getGroupName(), role.getGroupDn()))
                      .collect(Collectors.toList());
              roleGroupsMap.setGroups(groups);
              return roleGroupsMap;
            }).collect(Collectors.toList());
    roleGroupMapping.setRoleGroups(roleGroups);
    return roleGroupMapping;
  }

  private List<RoleGroupMappingEntity> convertRoleGroupMapping(RoleGroupMapping mapping) {

    final String providerTypeName = mapping.getProvider();
    final LdapProviderType providerTyper = LdapProviderType.fromValue(providerTypeName);
    return mapping.getRoleGroups().stream().flatMap(m -> {
      RoleEntity roleEntity = convertRole(m.getRole());
      return m.getGroups().stream().map(g -> {
        RoleGroupMappingEntity entity = new RoleGroupMappingEntity();
        entity.setGroupDn(g.getDistinguishedName());
        entity.setGroupName(g.getDisplayName());
        entity.setRole(roleEntity);
        entity.setProvider(providerTyper);
        return entity;
      });
    }).collect(Collectors.toList());
  }

  private RoleEntity convertRole(UserService.Role role) {
    RoleEntity roleEntity = new RoleEntity();
    roleEntity.setName(role.role);
    roleEntity.setId(role.id);
    return roleEntity;
  }

}
