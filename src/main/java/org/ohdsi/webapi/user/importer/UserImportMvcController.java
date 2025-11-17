package org.ohdsi.webapi.user.importer;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.arachne.scheduler.model.JobExecutingType;
import org.ohdsi.webapi.mvc.AbstractMvcController;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.ohdsi.webapi.Constants.JOB_IS_ALREADY_SCHEDULED;

/**
 * Spring MVC version of UserImportController
 *
 * Migration Status: Replaces /user/importer/UserImportController.java (Jersey)
 * Endpoints: 6 endpoints (GET, POST)
 * Complexity: Medium - user import from LDAP/AD with role mapping
 */
@RestController
@RequestMapping("/user")
public class UserImportMvcController extends AbstractMvcController {

    private static final Logger logger = LoggerFactory.getLogger(UserImportMvcController.class);

    private final UserImportService userImportService;
    private final UserImportJobService userImportJobService;
    private final GenericConversionService conversionService;

    @Value("${security.ad.url}")
    private String adUrl;

    @Value("${security.ldap.url}")
    private String ldapUrl;

    @Autowired
    public UserImportMvcController(UserImportService userImportService,
                                    UserImportJobService userImportJobService,
                                    GenericConversionService conversionService) {
        this.userImportService = userImportService;
        this.userImportJobService = userImportJobService;
        this.conversionService = conversionService;
    }

    /**
     * Get authentication providers
     *
     * Jersey: GET /WebAPI/user/providers
     * Spring MVC: GET /WebAPI/v2/user/providers
     *
     * @return authentication providers configuration
     */
    @GetMapping(
        value = "/providers",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthenticationProviders> getAuthenticationProviders() {
        AuthenticationProviders providers = new AuthenticationProviders();
        providers.setAdUrl(adUrl);
        providers.setLdapUrl(ldapUrl);
        return ok(providers);
    }

    /**
     * Test connection to LDAP/AD provider
     *
     * Jersey: GET /WebAPI/user/import/{type}/test
     * Spring MVC: GET /WebAPI/v2/user/import/{type}/test
     *
     * @param type provider type (ad or ldap)
     * @return connection test result
     */
    @GetMapping(
        value = "/import/{type}/test",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ConnectionInfo> testConnection(@PathVariable("type") String type) {
        LdapProviderType provider = LdapProviderType.fromValue(type);
        ConnectionInfo result = new ConnectionInfo();
        userImportService.testConnection(provider);
        result.setState(ConnectionInfo.ConnectionState.SUCCESS);
        result.setMessage("Connection success");
        return ok(result);
    }

    /**
     * Find groups in LDAP/AD
     *
     * Jersey: GET /WebAPI/user/import/{type}/groups
     * Spring MVC: GET /WebAPI/v2/user/import/{type}/groups
     *
     * @param type provider type (ad or ldap)
     * @param searchStr search string
     * @return list of LDAP groups
     */
    @GetMapping(
        value = "/import/{type}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<LdapGroup>> findGroups(
            @PathVariable("type") String type,
            @RequestParam(value = "search", required = false) String searchStr) {
        LdapProviderType provider = LdapProviderType.fromValue(type);
        return ok(userImportService.findGroups(provider, searchStr));
    }

    /**
     * Find users in directory
     *
     * Jersey: POST /WebAPI/user/import/{type}
     * Spring MVC: POST /WebAPI/v2/user/import/{type}
     *
     * @param type provider type (ad or ldap)
     * @param mapping role group mapping
     * @return list of Atlas user roles
     */
    @PostMapping(
        value = "/import/{type}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<AtlasUserRoles>> findDirectoryUsers(
            @PathVariable("type") String type,
            @RequestBody RoleGroupMapping mapping) {
        LdapProviderType provider = LdapProviderType.fromValue(type);
        return ok(userImportService.findUsers(provider, mapping));
    }

    /**
     * Import users from directory
     *
     * Jersey: POST /WebAPI/user/import
     * Spring MVC: POST /WebAPI/v2/user/import
     *
     * @param users list of Atlas user roles to import
     * @param provider provider type
     * @param preserveRoles whether to preserve existing roles
     * @return created user import job
     */
    @PostMapping(
        value = "/import",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserImportJobDTO> importUsers(
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
            return ok(conversionService.convert(created, UserImportJobDTO.class));
        } catch (JobAlreadyExistException e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    String.format(JOB_IS_ALREADY_SCHEDULED, jobDto.getProviderType()));
        }
    }

    /**
     * Save role group mapping
     *
     * Jersey: POST /WebAPI/user/import/{type}/mapping
     * Spring MVC: POST /WebAPI/v2/user/import/{type}/mapping
     *
     * @param type provider type (ad or ldap)
     * @param mapping role group mapping
     */
    @PostMapping(
        value = "/import/{type}/mapping",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> saveMapping(@PathVariable("type") String type, @RequestBody RoleGroupMapping mapping) {
        LdapProviderType providerType = LdapProviderType.fromValue(type);
        List<RoleGroupEntity> mappingEntities = RoleGroupMappingConverter.convertRoleGroupMapping(mapping);
        userImportService.saveRoleGroupMapping(providerType, mappingEntities);
        return ok();
    }

    /**
     * Get role group mapping
     *
     * Jersey: GET /WebAPI/user/import/{type}/mapping
     * Spring MVC: GET /WebAPI/v2/user/import/{type}/mapping
     *
     * @param type provider type (ad or ldap)
     * @return role group mapping
     */
    @GetMapping(
        value = "/import/{type}/mapping",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RoleGroupMapping> getMapping(@PathVariable("type") String type) {
        LdapProviderType providerType = LdapProviderType.fromValue(type);
        List<RoleGroupEntity> mappingEntities = userImportService.getRoleGroupMapping(providerType);
        return ok(RoleGroupMappingConverter.convertRoleGroupMapping(type, mappingEntities));
    }

    private Date getJobStartDate() {
        Calendar calendar = GregorianCalendar.getInstance();
        // Job will be started in five seconds after now
        calendar.add(Calendar.SECOND, 5);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
