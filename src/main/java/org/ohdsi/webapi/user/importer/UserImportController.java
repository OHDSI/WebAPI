package org.ohdsi.webapi.user.importer;

import com.odysseusinc.scheduler.model.JobExecutingType;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.user.importer.converter.RoleGroupMappingConverter;
import org.ohdsi.webapi.user.importer.dto.UserImportJobDTO;
import org.ohdsi.webapi.user.importer.exception.JobAlreadyExistException;
import org.ohdsi.webapi.user.importer.model.AtlasUserRoles;
import org.ohdsi.webapi.user.importer.model.AuthenticationProviders;
import org.ohdsi.webapi.user.importer.model.ConnectionInfo;
import org.ohdsi.webapi.user.importer.model.LdapGroup;
import org.ohdsi.webapi.user.importer.model.LdapProviderType;
import org.ohdsi.webapi.user.importer.model.RoleGroupEntity;
import org.ohdsi.webapi.user.importer.model.RoleGroupMapping;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.ohdsi.webapi.user.importer.service.UserImportJobService;
import org.ohdsi.webapi.user.importer.service.UserImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.ohdsi.webapi.Constants.JOB_IS_ALREADY_SCHEDULED;

@Controller
@Path("/")
public class UserImportController {

  private static final Logger logger = LoggerFactory.getLogger(UserImportController.class);

  @Autowired
  private UserImportService userImportService;

  @Autowired
  private UserImportJobService userImportJobService;

  @Autowired
  private GenericConversionService conversionService;

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
    userImportService.testConnection(provider);
    result.setState(ConnectionInfo.ConnectionState.SUCCESS);
    result.setMessage("Connection success");
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
    @Produces(MediaType.APPLICATION_JSON)
    public UserImportJobDTO importUsers(List<AtlasUserRoles> users,
                                        @QueryParam("provider") String provider,
                                        @DefaultValue("TRUE") @QueryParam("preserve") Boolean preserveRoles) {
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
            throw new NotAcceptableException(String.format(JOB_IS_ALREADY_SCHEDULED, jobDto.getProviderType()));
        }
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

    private Date getJobStartDate() {
        Calendar calendar = GregorianCalendar.getInstance();
        // Job will be started in five seconds after now
        calendar.add(Calendar.SECOND, 5);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
