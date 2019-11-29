package com.jnj.honeur.webapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jnj.honeur.security.SecurityUtils2;
import com.jnj.honeur.webapi.SourceDaimonContextHolder;
import com.jnj.honeur.webapi.cohortdefinition.CohortGenerationResults;
import com.jnj.honeur.webapi.cohortfeatures.CohortFeaturesEntity;
import com.jnj.honeur.webapi.cohortfeatures.CohortFeaturesRepository;
import com.jnj.honeur.webapi.cohortfeaturesanalysisref.CohortFeaturesAnalysisRefEntity;
import com.jnj.honeur.webapi.cohortfeaturesanalysisref.CohortFeaturesAnalysisRefRepository;
import com.jnj.honeur.webapi.cohortfeaturesdist.CohortFeaturesDistEntity;
import com.jnj.honeur.webapi.cohortfeaturesdist.CohortFeaturesDistRepository;
import com.jnj.honeur.webapi.cohortfeaturesref.CohortFeaturesRefEntity;
import com.jnj.honeur.webapi.cohortfeaturesref.CohortFeaturesRefRepository;
import com.jnj.honeur.webapi.cohortinclusion.CohortInclusionEntity;
import com.jnj.honeur.webapi.cohortinclusion.CohortInclusionRepository;
import com.jnj.honeur.webapi.cohortinclusionresult.CohortInclusionResultEntity;
import com.jnj.honeur.webapi.cohortinclusionresult.CohortInclusionResultRepository;
import com.jnj.honeur.webapi.cohortinclusionstats.CohortInclusionStatsEntity;
import com.jnj.honeur.webapi.cohortinclusionstats.CohortInclusionStatsRepository;
import com.jnj.honeur.webapi.cohortsummarystats.CohortSummaryStatsEntity;
import com.jnj.honeur.webapi.cohortsummarystats.CohortSummaryStatsRepository;
import com.jnj.honeur.webapi.hss.CohortDefinitionStorageInformationItem;
import com.jnj.honeur.webapi.hss.StorageInformationItem;
import com.jnj.honeur.webapi.hss.StorageServiceClient;
import com.jnj.honeur.webapi.liferay.LiferayApiClient;
import com.jnj.honeur.webapi.liferay.model.Organization;
import com.jnj.honeur.webapi.shiro.LiferayPermissionManager;
import com.jnj.honeur.webapi.source.SourceDaimonContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.cohort.CohortEntity;
import org.ohdsi.webapi.cohort.CohortRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfoRepository;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.RoleRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Path("/cohortdefinition")
@Component
@ConditionalOnProperty(name = "datasource.honeur.enabled", havingValue = "true")
public class HoneurCohortDefinitionServiceExtension {

    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    private Security security;

    @Autowired(required = false)
    private LiferayPermissionManager authorizer;

    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;


    @Autowired(required = false)
    private LiferayApiClient liferayApiClient;

    @Autowired
    private StorageServiceClient storageServiceClient;

    @Autowired
    private CohortDefinitionService cohortDefinitionService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private CohortGenerationInfoRepository cohortGenerationInfoRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private CohortInclusionRepository cohortInclusionRepository;

    @Autowired
    private CohortInclusionStatsRepository cohortInclusionStatsRepository;

    @Autowired
    private CohortInclusionResultRepository cohortInclusionResultRepository;

    @Autowired
    private CohortSummaryStatsRepository cohortSummaryStatsRepository;

    @Autowired
    private CohortFeaturesRepository cohortFeaturesRepository;

    @Autowired
    private CohortFeaturesAnalysisRefRepository cohortFeaturesAnalysisRefRepository;

    @Autowired
    private CohortFeaturesDistRepository cohortFeaturesDistRepository;

    @Autowired
    private CohortFeaturesRefRepository cohortFeaturesRefRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired(required = false)
    private CohortGenerationImportService cohortGenerationImportService;

    private final ObjectMapper mapper = new ObjectMapper();


    /**
     * Returns all cohort definitions in amazon to which the user has access
     *
     * @return List of cohort_definition
     */
    @GET
    @Path("/hss/list/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CohortDefinitionStorageInformationItem> getCohortDefinitionImportList(@HeaderParam("Authorization") String token, @CookieParam("userFingerprint") String userFingerprint) {
        return storageServiceClient.getCohortDefinitionImportList(token, userFingerprint);
    }

    /**
     * Returns all cohort definition results in amazon to which the user has access
     *
     * @param id The id of the definition in WebAPI.
     * @return List of cohort_definition
     */
    @GET
    @Path("/hss/list/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StorageInformationItem> getCohortDefinitionResultsImportList(@HeaderParam("Authorization") String token, @CookieParam("userFingerprint") String userFingerprint, @PathParam("id") final int id) {
        if (cohortDefinitionRepository.findOne(id) == null || cohortDefinitionRepository.findOne(id).getUuid() == null) {
            throw new IllegalArgumentException(String.format("Definition with ID=%s does not exist or is not yet shared!", id));
        }
        return storageServiceClient.getCohortDefinitionResultsImportList(token, userFingerprint, cohortDefinitionRepository.findOne(id).getUuid());
    }

    /**
     * Creates the cohort definition based on an imported JSON from HSS
     *
     * @param storageInformationItem The cohort definition item as defined by HSS
     * @return The new CohortDefinition
     */
    @POST
    @Path("/hss/select")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public CohortDefinitionService.CohortDefinitionDTO createCohortDefinitionFromFile(@HeaderParam("Authorization") String token, @CookieParam("userFingerprint") String userFingerprint, final StorageInformationItem storageInformationItem) {
        CohortDefinitionService.CohortDefinitionDTO
                definition = storageServiceClient.getCohortDefinition(token, userFingerprint, storageInformationItem.getUuid());

        definition.id = null;
        definition.organizations = new ArrayList<>();

        return cohortDefinitionService.createCohortDefinition(definition);
    }

    /**
     * Creates the cohort definition generation results based on an imported JSON from HSS
     *
     * @param id The id of the cohort definition in WebAPI
     * @param sourceKey The key of the datasource for which to import results
     * @param storageInformationItem The results item as presented by HSS
     * @return The new CohortDefinitionGenerationResults
     */
    @POST
    @Path("/hss/{id}/select/{sourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public JobExecutionResource createCohortDefinitionResultsFromFile(@HeaderParam("Authorization") String token, @CookieParam("userFingerprint") String userFingerprint, @PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, final StorageInformationItem storageInformationItem) throws Exception {
        CohortGenerationResults results = storageServiceClient.getCohortGenerationResults(token, userFingerprint,
                cohortDefinitionRepository.findOne(id).getUuid().toString(), storageInformationItem.getUuid());
        return importCohortResults(token, id, sourceKey, results);
    }


    /**
     * Exports the results of the generation of a cohort task for the specified cohort definition.
     *
     * @param id - the Cohort Definition ID to export results for.
     * @param sourceKey - the database for which to export these results.
     * @return information about the Cohort Analysis Job
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/export/{sourceKey}")
    public Response exportCohortResults(@HeaderParam("Authorization") String token, @CookieParam("userFingerprint") String userFingerprint, @PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, @QueryParam("toCloud") final boolean toCloud, HttpHeaders headers) {
        try {
            log.info("Cohort definition id: " + id);
            log.info("Source key: " + sourceKey);
            log.info("toCloud: " + toCloud);

            CohortGenerationInfo info = this.cohortGenerationInfoRepository.findGenerationInfoByIdAndSourceId(id, sourceRepository.findBySourceKey(sourceKey).getSourceId());
            log.info("CohortGenerationInfo: " + info);

            log.info("Set source daimon context to: " + new SourceDaimonContext(sourceKey, SourceDaimon.DaimonType.Results).getSourceDaimonContextKey());
            SourceDaimonContextHolder
                    .setCurrentSourceDaimonContext(new SourceDaimonContext(sourceKey, SourceDaimon.DaimonType.Results));

            log.debug("cohortRepository.getAllCohortsForId: " + id);
            List<CohortEntity> cohorts = cohortRepository.getAllCohortsForId((long) id);
            log.debug("cohortInclusionRepository.findByCohortDefinitionId: " + id);
            List<CohortInclusionEntity> cohortInclusions =
                    cohortInclusionRepository.findByCohortDefinitionId((long) id);
            log.debug("cohortInclusionResultRepository.findByCohortDefinitionId: " + id);
            List<CohortInclusionResultEntity> cohortInclusionResults =
                    cohortInclusionResultRepository.findByCohortDefinitionId((long) id);
            log.debug("cohortInclusionStatsRepository.findByCohortDefinitionId: " + id);
            List<CohortInclusionStatsEntity> cohortInclusionStats =
                    cohortInclusionStatsRepository.findByCohortDefinitionId((long) id);
            log.debug("cohortSummaryStatsRepository.findByCohortDefinitionId: " + id);
            List<CohortSummaryStatsEntity> cohortSummaryStats =
                    cohortSummaryStatsRepository.findByCohortDefinitionId((long) id);

            CohortGenerationResults results = new CohortGenerationResults();
            results.setCohort(cohorts);
            results.setCohortInclusion(cohortInclusions);
            results.setCohortInclusionResult(cohortInclusionResults);
            results.setCohortInclusionStats(cohortInclusionStats);
            results.setCohortSummaryStats(cohortSummaryStats);

            log.info("Include features? " + info.isIncludeFeatures());
            if(info.isIncludeFeatures()){
                List<CohortFeaturesEntity> cohortFeaturesEntities = cohortFeaturesRepository.findByCohortDefinitionId((long) id);
                List<CohortFeaturesAnalysisRefEntity> cohortFeaturesAnalysisRefEntities = cohortFeaturesAnalysisRefRepository.findByCohortDefinitionId((long) id);
                List<CohortFeaturesDistEntity> cohortFeaturesDistEntities = cohortFeaturesDistRepository.findByCohortDefinitionId((long) id);
                List<CohortFeaturesRefEntity> cohortFeaturesRefEntities = cohortFeaturesRefRepository.findByCohortDefinitionId((long) id);

                results.setCohortFeatures(cohortFeaturesEntities);
                results.setCohortFeaturesAnalysisRef(cohortFeaturesAnalysisRefEntities);
                results.setCohortFeaturesDist(cohortFeaturesDistEntities);
                results.setCohortFeaturesRef(cohortFeaturesRefEntities);
            }

            results.setCohortGenerationInfo(info);

            SourceDaimonContextHolder.clear();

            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            File file = createFile(sourceKey+"-"+timeStamp+".results", results);

            log.debug("cohortDefinitionRepository.findOneWithDetail: " + id);
            CohortDefinition cohortDefinition = this.cohortDefinitionRepository.findOneWithDetail(id);
            log.debug("Cohort definition with details: " + cohortDefinition);

            if(toCloud && file != null) {
                log.debug("Export cohort results to HSS");
                if(cohortDefinition.getUuid() == null) {
                    log.error("Unable to export a cohort definition without a UUID!");
                    return Response.serverError().build();
                }
                storageServiceClient.saveResults(token, userFingerprint, file, cohortDefinition.getUuid().toString());
            }
            return getResponse(file);
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            return Response.serverError().build();
        } finally {
            SourceDaimonContextHolder.clear();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/export")
    @Transactional
    public Response exportCohortDefinition(@HeaderParam("Authorization") String token, @CookieParam("userFingerprint") String userFingerprint, @Context HttpServletRequest request, @PathParam("id") final int id, @QueryParam("toCloud") final boolean toCloud) {
        CohortDefinition cohortDefinition = this.cohortDefinitionRepository.findOneWithDetail(id);

//        String expression = cohortDefinition.getDetails().getExpression();

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        UUID uuid = UUID.randomUUID();
        cohortDefinition.setUuid(uuid);
        File file = createFile(cohortDefinition.getName()+"-"+timeStamp+".cohort", this.cohortDefinitionService.cohortDefinitionToDTO(cohortDefinition));
        if(toCloud){
            if(file != null) {
                if (cohortDefinition.getGroupKey() == null) {
                    UUID groupKey = UUID.randomUUID();
                    storageServiceClient.saveCohort(token, userFingerprint, file, groupKey, uuid);
                    cohortDefinition.setGroupKey(groupKey);
                } else {
                    storageServiceClient.saveCohort(token, userFingerprint, file, cohortDefinition.getGroupKey(), uuid);
                }
                this.cohortDefinitionRepository.save(cohortDefinition);
            }

            //Copy definition
            CohortDefinition newDef = new CohortDefinition();
            newDef.setPreviousVersion(cohortDefinition);
            newDef.setCreatedBy(cohortDefinition.getCreatedBy());
            newDef.setCreatedDate(cohortDefinition.getCreatedDate());
            newDef.setDescription(cohortDefinition.getDescription());
            newDef.setDetails(cohortDefinition.getDetails());
            newDef.setExpressionType(cohortDefinition.getExpressionType());
            newDef.setGenerationInfoList(cohortDefinition.getGenerationInfoList());
            newDef.setModifiedBy(cohortDefinition.getModifiedBy());
            newDef.setModifiedDate(cohortDefinition.getModifiedDate());
            newDef.setName(cohortDefinition.getName());
            newDef.setGroupKey(cohortDefinition.getGroupKey());

            CohortDefinitionService.CohortDefinitionDTO  toReturn = this.cohortDefinitionService.cohortDefinitionToDTO(newDef);
            CohortDefinitionService.CohortDefinitionDTO copyDef = this.cohortDefinitionService.createCohortDefinition(toReturn);

            List<Organization> organizations = getAllOrganisations(id);
            savePermissionsForOrganizations(copyDef.id, organizations);

            return getResponse(createFile(copyDef.name+"-"+timeStamp+".cohort", copyDef));
        }

        return getResponse(createFile(cohortDefinition.getName()+"-"+timeStamp+".cohort",this.cohortDefinitionService.cohortDefinitionToDTO(cohortDefinition)));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/organizations")
    public List<Organization> savePermissionsForOrganizations(@PathParam("id") final int id, final List<Organization> organizations){
        CohortDefinition def = cohortDefinitionRepository.findOneWithDetail(id);
        //Create organisation roles
        List<RoleEntity> organisationsWithRoles = createRoleEntities(organizations, def);

        //Add organization to role in liferay
        List<Organization> organizationsToAddToRole = organisationsWithRoles.stream()
                .map(roleEntity -> organizations.stream()
                        .filter(organization -> organization.getName().equals(roleEntity.getName()) )
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

//    this.authorizer.addOrganization(organizationsToAddToRole);

        return organizationsToAddToRole;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/organizations")
    public List<Organization> getAllOrganisations(@PathParam("id") final int id) {
        List<Organization> organizations = this.liferayApiClient.getOrganizations();
        Iterable<RoleEntity> roleEntities = this.authorizer.getRoles(false);

        List<String> rolesWithPermissionToReadCohortdefinition;
        List<Organization> finalOrganizations = organizations;
        if (id == 0){
            rolesWithPermissionToReadCohortdefinition = new ArrayList<>();
        } else {
            rolesWithPermissionToReadCohortdefinition = StreamSupport.stream(roleEntities.spliterator(), false)
                    .filter(roleEntity -> {
                        List<PermissionEntity> permissions = new ArrayList<>();
                        try {
                            permissions = this.authorizer.getRolePermissions(roleEntity.getId()).stream()
                                    .filter(permissionEntity -> permissionEntity.getValue()
                                            .equals("cohortdefinition:" + id + ":get"))
                                    .collect(Collectors.toList());

                        } catch (Exception e) {
                        }
                        return permissions.size() > 0;
                    })
                    .filter(roleEntity -> finalOrganizations.stream()
                            .map(Organization::getName)
                            .collect(Collectors.toList())
                            .contains(roleEntity.getName()))
                    .map(RoleEntity::getName)
                    .collect(Collectors.toList());
        }

        organizations = organizations.stream()
                .map(organization -> {
                    organization.setCanRead(rolesWithPermissionToReadCohortdefinition.contains(organization.getName()));
                    return organization;
                }).collect(Collectors.toList());
        return organizations;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/uuids")
    public List<String> getUUIDSList(@HeaderParam("token") String token, @CookieParam("userFingerprint") String userFingerprint){
        log.info("Path: /cohortdefinition/uuids");
        final Set<PermissionEntity> userPermissions = this.authorizer.getUserPermissions(SecurityUtils2.getSubject(token, userFingerprint));

        if(userHasCohortDefinitionWildcardPermission(userPermissions)) {
            return getCohortDefinitionUuids(cohortDefinitionRepository.findAll());
        }

        List<Integer> cohortDefinitionIds = getCohortDefinitionIds(userPermissions);
        if(!cohortDefinitionIds.isEmpty()) {
            return getCohortDefinitionUuids(cohortDefinitionRepository.findFromList(cohortDefinitionIds));
        }

        return Collections.emptyList();
    }

    private List<Integer> getCohortDefinitionIds(Set<PermissionEntity> userPermissions) {
        String permissionPattern = "cohortdefinition:([0-9]*):get";
        List<Integer> definitionIds = userPermissions.stream()
                .map(PermissionEntity::getValue)
                .filter(permissionString -> permissionString.matches(permissionPattern))
                .map(permissionString -> parseCohortDefinitionId(permissionString))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        log.info(String.format("User has access to : %s", definitionIds));
        return definitionIds;
    }

    private Optional<Integer> parseCohortDefinitionId(String permission) {
        try {
            return Optional.of(Integer.parseInt(permission.split(":")[1]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private boolean userHasCohortDefinitionWildcardPermission(Set<PermissionEntity> userPermissions) {
        String wildcardPermissionPattern = "cohortdefinition:*:get";
        return userPermissions.stream()
                .map(PermissionEntity::getValue)
                .anyMatch(permissionString -> permissionString.equals(wildcardPermissionPattern));
    }

    private List<String> getCohortDefinitionUuids(final Iterable<CohortDefinition> cohortDefinitions) {
        return StreamSupport.stream(cohortDefinitions.spliterator(), false)
                .map(CohortDefinition::getUuid)
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .collect(Collectors.toList());
    }

    /**
     * Imports the results of the generation of a cohort task for the specified cohort definition.
     *
     * @param id - the Cohort Definition ID to import results for.
     * @param sourceKey - the database for which to import these results.
     * @return information about the Cohort Analysis Job
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/import/{sourceKey}")
    @Transactional
    public JobExecutionResource importCohortResults(@HeaderParam("Authorization") String token, @PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, CohortGenerationResults cohortGenerationResults){
        return cohortGenerationImportService.importCohortGeneration(id, cohortGenerationResults, sourceKey);
//        try {
//            SourceDaimonContextHolder.setCurrentSourceDaimonContext(new SourceDaimonContext(sourceKey, SourceDaimon.DaimonType.Results));
//            CohortGenerationResults results = importCohortGenerationResults(id, cohortGenerationResults);
//            SourceDaimonContextHolder.clear();
//
//            results.setCohortGenerationInfo(
//                    importCohortGenerationInfo(id, sourceKey, cohortGenerationResults.getCohortGenerationInfo()));
//
//            addViewPermissions(token, id, sourceKey);
//
//            return results;
//
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            // TODO: better return
//            return new CohortGenerationResults();
//        } finally {
//            SourceDaimonContextHolder.clear();
//        }
    }


    private File createFile(String fileName, Object results) {
        File tempFile = null;
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            log.info(tempDir);
            //String tempDir = new File(System.getenv("temp")).getAbsolutePath();
            tempFile = new File(tempDir + "/" + fileName); //We can add timestamp in path to make it unique
            mapper.writeValue(tempFile, results);
        } catch (IOException e){
            log.error(e.getMessage(), e);
        }
        return tempFile;
    }

    private Response getResponse(final File file) {
        return Response
                .ok(file)
                .header("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()))
                .build();
    }

    /**
     * Create roles for organizations to which a cohort definition is shared.
     * Add the appropriate permissions to those created roles.
     *
     * @param organizations
     * @param newDef
     * @return
     */
    private List<RoleEntity> createRoleEntities(List<Organization> organizations,
                                                CohortDefinition newDef) {
        if (organizations == null) {
            return new ArrayList<>();
        }

        Iterable<RoleEntity> roles = this.authorizer.getRoles(false);

        List<RoleEntity> existingOrganizationRolesPermissionAdd = organizations.stream()
                .map(organisation -> StreamSupport.stream(roles.spliterator(), false)
                        .filter(roleEntity -> roleEntity.getName().equals(organisation.getName()) && organisation.isCanRead())
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<RoleEntity> existingOrganizationRolesPermissionRemove = organizations.stream()
                .map(organisation -> StreamSupport.stream(roles.spliterator(), false)
                        .filter(roleEntity -> roleEntity.getName().equals(organisation.getName()) && !organisation.isCanRead())
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Organization> organisationsWithoutRoles = organizations.stream()
                .filter(organisation -> organisation.isCanRead() && StreamSupport.stream(roles.spliterator(), false)
                        .filter(roleEntity -> roleEntity.getName().equals(organisation.getName()))
                        .collect(Collectors.toList()).size() == 0)
                .collect(Collectors.toList());

        List<PermissionEntity> entities = new ArrayList<>();
        entities.add(this.authorizer.getPermissionByValue("cohortdefinition:" + newDef.getId() + ":get"));
        entities.add(this.authorizer.getPermissionByValue("cohortdefinition:" + newDef.getId() + ":info:get"));
        entities.add(this.authorizer.getPermissionByValue("cohortdefinition:sql:post"));

        for (Organization org: organisationsWithoutRoles) {
            RoleEntity roleEntity = this.authorizer.addOrganizationRole(org.getName());
            if(roleEntity != null){
                existingOrganizationRolesPermissionAdd.add(roleEntity);
            } else {
                log.info("CREATION OF ORGANIZATION ROLE FAILED: Role is null");
            }
        }

        for(PermissionEntity permissionEntity: entities) {
            for (RoleEntity role : existingOrganizationRolesPermissionAdd) {
                if (permissionEntity != null) {
                    this.authorizer.addPermission(role, permissionEntity);
                }
            }

            for (RoleEntity role : existingOrganizationRolesPermissionRemove) {
                if (permissionEntity != null) {
                    this.authorizer.removePermission(permissionEntity.getId(), role.getId());
                }
            }
        }

        return existingOrganizationRolesPermissionAdd;
    }

    @Autowired()
    private RequestMappingHandlerMapping requestMappingHandlerMapping;


    @PostConstruct
    public void initIt() throws Exception {

        Object[] list = requestMappingHandlerMapping.getHandlerMethods().keySet().stream().map(t ->
                (t.getMethodsCondition().getMethods().size() == 0 ? "GET" : t.getMethodsCondition().getMethods().toArray()[0]) + " " +
                        t.getPatternsCondition().getPatterns().toArray()[0]
        ).toArray();

        for(Object o: list){
            System.out.println(o.toString());
        }
        System.out.println("HONEUR COHORT DEFINITION SERVICE EXTENSION CREATED");
    }
}
