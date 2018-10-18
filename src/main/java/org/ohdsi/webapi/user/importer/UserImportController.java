package org.ohdsi.webapi.user.importer;

import org.ohdsi.webapi.user.importer.converter.RoleGroupMappingConverter;
import org.ohdsi.webapi.user.importer.model.*;
import org.ohdsi.webapi.user.importer.service.UserImportJobService;
import org.ohdsi.webapi.user.importer.service.UserImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Controller
@Path("/")
public class UserImportController {

  private static final Logger logger = LoggerFactory.getLogger(UserImportController.class);

  @Autowired
  private UserImportService userImportService;

  @Autowired
  private UserImportJobService userImportJobService;

  @Value("${security.ad.url}")
  private String adUrl;

  @Value("${security.ldap.url}")
  private String ldapUrl;

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
      userImportService.testConnection(provider);
      result.setState(ConnectionInfo.ConnectionState.SUCCESS);
      result.setMessage("Connection success");
    } catch(Exception e) {
      logger.error("LDAP connection failed", e);
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
    return userImportService.findGroups(provider, searchStr);
  }

  @POST
  @Path("user/import/{type}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<AtlasUserRoles> findDirectoryUsers(@PathParam("type") String type, RoleGroupMapping mapping){
    LdapProviderType provider = LdapProviderType.fromValue(type);
    return userImportService.findUsers(provider, mapping);
  }

  @POST
  @Path("user/import")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response importUsers(List<AtlasUserRoles> users,
                              @QueryParam("provider") String provider,
                              @DefaultValue("TRUE") @QueryParam("preserve") Boolean preserveRoles) {

    LdapProviderType providerType = LdapProviderType.fromValue(provider);
    userImportJobService.runImportUsersTask(providerType, users, preserveRoles);
    return Response.ok().build();
  }

  @POST
  @Path("user/import/{type}/mapping")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response saveMapping(@PathParam("type") String type, RoleGroupMapping mapping) {
    LdapProviderType providerType = LdapProviderType.fromValue(type);
    List<RoleGroupEntity> mappingEntities = RoleGroupMappingConverter.convertRoleGroupMapping(mapping);
    userImportService.saveRoleGroupMapping(providerType, mappingEntities);
    return Response.ok().build();
  }

  @GET
  @Path("user/import/{type}/mapping")
  @Produces(MediaType.APPLICATION_JSON)
  public RoleGroupMapping getMapping(@PathParam("type") String type) {
    LdapProviderType providerType = LdapProviderType.fromValue(type);
    List<RoleGroupEntity> mappingEntities = userImportService.getRoleGroupMapping(providerType);
    return RoleGroupMappingConverter.convertRoleGroupMapping(type, mappingEntities);
  }

}
