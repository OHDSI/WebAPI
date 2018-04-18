package com.jnj.honeur.webapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jnj.honeur.webapi.SourceDaimonContextHolder;
import com.jnj.honeur.webapi.cohortdefinition.CohortGenerationInfoRepository;
import com.jnj.honeur.webapi.cohortdefinition.CohortGenerationResults;
import com.jnj.honeur.webapi.cohortinclusion.CohortInclusionEntity;
import com.jnj.honeur.webapi.cohortinclusion.CohortInclusionRepository;
import com.jnj.honeur.webapi.cohortinclusionresult.CohortInclusionResultEntity;
import com.jnj.honeur.webapi.cohortinclusionresult.CohortInclusionResultRepository;
import com.jnj.honeur.webapi.cohortinclusionstats.CohortInclusionStatsEntity;
import com.jnj.honeur.webapi.cohortinclusionstats.CohortInclusionStatsRepository;
import com.jnj.honeur.webapi.cohortsummarystats.CohortSummaryStatsEntity;
import com.jnj.honeur.webapi.cohortsummarystats.CohortSummaryStatsRepository;
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
import org.ohdsi.webapi.cohortdefinition.ExpressionType;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.source.SourceDaimon;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Path("/cohortdefinition")
@Component
@ConditionalOnProperty(name = "datasource.honeur.enabled", havingValue = "true")
public class HoneurCohortDefinitionServiceExtension {


    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    private LiferayPermissionManager authorizer;

    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;


    @Autowired
    private LiferayApiClient liferayApiClient;

    @Autowired
    private StorageServiceClient storageServiceClient;

    @Autowired
    private CohortDefinitionService cohortDefinitionService;

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

    private final ObjectMapper mapper = new ObjectMapper();


    /**
     * Returns all cohort definitions in amazon to which the user has access
     *
     * @return List of cohort_definition
     */
    @GET
    @Path("/hss/list/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StorageInformationItem> getCohortDefinitionImportList() {
        return storageServiceClient.getCohortDefinitionImportList();
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
    public List<StorageInformationItem> getCohortDefinitionResultsImportList(@PathParam("id") final int id) {
        return storageServiceClient.getCohortDefinitionResultsImportList(cohortDefinitionRepository.findOne(id).getUuid().toString());
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
    public CohortDefinitionService.CohortDefinitionDTO createCohortDefinitionFromFile(final StorageInformationItem storageInformationItem) {
        String expression = storageServiceClient.getCohortDefinition(storageInformationItem.getUuid());

        CohortDefinitionService.CohortDefinitionDTO def = new CohortDefinitionService.CohortDefinitionDTO();
        def.expression = expression;
        def.name = storageInformationItem.getOriginalFilename().replace(".cohort","");
        def.expressionType = ExpressionType.SIMPLE_EXPRESSION;
        def.organizations = new ArrayList<>();
        def.uuid = UUID.fromString(storageInformationItem.getUuid());

        return cohortDefinitionService.createCohortDefinition(def);
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
    public CohortGenerationResults createCohortDefinitionResultsFromFile(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, final StorageInformationItem storageInformationItem) throws Exception {
        CohortGenerationResults results = storageServiceClient.getCohortGenerationResults(
                cohortDefinitionRepository.findOne(id).getUuid().toString(), storageInformationItem.getUuid());
        return importCohortResults(id, sourceKey, results);
    }


    /**
     * Exports the results of the generation of a cohort task for the specified cohort definition.
     *
     * @param id - the Cohort Definition ID to export results for.
     * @param sourceKey - the database for which to export these results.
     * @return information about the Cohort Analysis Job
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{id}/export/{sourceKey}")
    public Response exportCohortResults(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, @QueryParam("toCloud") final boolean toCloud, @QueryParam("uuid") final String uuid, HttpHeaders headers) {
        SourceDaimonContextHolder.setCurrentSourceDaimonContext(new SourceDaimonContext(sourceKey, SourceDaimon.DaimonType.Results));

        List<CohortEntity> cohorts = cohortRepository.getAllCohortsForId((long) id);
        List<CohortInclusionEntity> cohortInclusions = cohortInclusionRepository.getAllCohortInclusionsForId((long) id);
        List<CohortInclusionResultEntity> cohortInclusionResults = cohortInclusionResultRepository.getAllCohortInclusionResultsForId((long) id);
        List<CohortInclusionStatsEntity> cohortInclusionStats = cohortInclusionStatsRepository.getAllCohortInclusionStatsForId((long) id);
        List<CohortSummaryStatsEntity> cohortSummaryStats = cohortSummaryStatsRepository.getAllCohortInclusionSummaryStatsForId((long) id);

        SourceDaimonContextHolder.clear();

        List<CohortGenerationInfo> infos = this.cohortGenerationInfoRepository.listGenerationInfoById(id);

        CohortGenerationResults results = new CohortGenerationResults();
        results.cohort = cohorts;
        results.cohortInclusion = cohortInclusions;
        results.cohortInclusionResult = cohortInclusionResults;
        results.cohortInclusionStats = cohortInclusionStats;
        results.cohortSummaryStats = cohortSummaryStats;
        results.cohortGenerationInfo = infos;


        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File file = createFile(sourceKey+"-"+timeStamp+".results", results);

        if(toCloud && file != null){
            storageServiceClient.saveResults(file,uuid);
        }
        return getResponse(file);
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{id}/export")
    public Response getCohortDefinitionExpression(@Context HttpServletRequest request, @PathParam("id") final int id, @QueryParam("toCloud") final boolean toCloud) {
        CohortDefinition cohortDefinition = this.cohortDefinitionRepository.findOneWithDetail(id);

        String expression = cohortDefinition.getDetails().getExpression();

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File file = createFile(cohortDefinition.getName()+"-"+timeStamp+".cohort", expression);
        if(toCloud && file != null){
            String uuid = storageServiceClient.saveCohort(file);
            cohortDefinition.setUuid(UUID.fromString(uuid));
            this.cohortDefinitionRepository.save(cohortDefinition);
        }
        return getResponse(file);
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
                .map(roleEntity -> StreamSupport.stream(organizations.spliterator(), false)
                        .filter(organization -> organization.getName().equals(roleEntity.getName()) )
                        .findFirst().orElse(null))
                .filter(organization -> organization != null)
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

        List<Organization> finalOrganizations = organizations;
        List<String> rolesWithPermissionToReadCohortdefinition = StreamSupport.stream(roleEntities.spliterator(), false)
                .filter(roleEntity -> {
                    List<PermissionEntity> permissions = new ArrayList<>();
                    try {
                        permissions = this.authorizer.getRolePermissions(roleEntity.getId()).stream()
                                .filter(permissionEntity -> permissionEntity.getValue().equals("cohortdefinition:"+id+":get"))
                                .collect(Collectors.toList());

                    } catch (Exception e) {
                    }
                    return permissions.size() > 0;
                })
                .filter(roleEntity -> finalOrganizations.stream()
                        .map(organization -> organization.getName())
                        .collect(Collectors.toList())
                        .contains(roleEntity.getName()))
                .map(roleEntity -> roleEntity.getName())
                .collect(Collectors.toList());

        organizations = organizations.stream()
                .map(organization -> {
                    organization.setCanRead(rolesWithPermissionToReadCohortdefinition.contains(organization.getName()));
                    return organization;
                }).collect(Collectors.toList());
        return organizations;
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
    public CohortGenerationResults importCohortResults(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, CohortGenerationResults cohortGenerationResults){
        SourceDaimonContextHolder.setCurrentSourceDaimonContext(new SourceDaimonContext(sourceKey, SourceDaimon.DaimonType.Results));
        CohortGenerationResults results = importCohortGenerationResults(id, cohortGenerationResults);
        SourceDaimonContextHolder.clear();
        results.cohortGenerationInfo = importCohortGenerationInfo(id, sourceKey, cohortGenerationResults.cohortGenerationInfo);
        return results;
    }

    @Transactional
    CohortGenerationResults importCohortGenerationResults(int id, CohortGenerationResults cohortGenerationResults) {
        List<CohortEntity> cohortEntities = new ArrayList<>();
        for(CohortEntity cohort: cohortGenerationResults.cohort){
            CohortEntity cohortEntity = new CohortEntity();
            cohortEntity.setCohortDefinitionId((long)id);
            cohortEntity.setCohortEndDate(cohort.getCohortEndDate());
            cohortEntity.setCohortStartDate(cohort.getCohortStartDate());
            cohortEntity.setSubjectId(cohort.getSubjectId());
            cohortEntities.add(cohortEntity);
        }
        cohortRepository.save(cohortEntities);

        List<CohortInclusionEntity> cohortInclusionEntities = new ArrayList<>();
        for(CohortInclusionEntity cohortInclusion: cohortGenerationResults.cohortInclusion){
            CohortInclusionEntity cohortInclusionEntity = new CohortInclusionEntity();
            cohortInclusionEntity.setCohortDefinitionId((long) id);
            cohortInclusionEntity.setDescription(cohortInclusion.getDescription());
            cohortInclusionEntity.setName(cohortInclusion.getName());
            cohortInclusionEntity.setRuleSequence(cohortInclusion.getRuleSequence());
            cohortInclusionEntities.add(cohortInclusionEntity);
        }
        cohortInclusionRepository.save(cohortInclusionEntities);

        List<CohortInclusionResultEntity> cohortInclusionResultEntities = new ArrayList<>();
        for(CohortInclusionResultEntity cohortInclusionResult: cohortGenerationResults.cohortInclusionResult){
            CohortInclusionResultEntity cohortInclusionResultEntity = new CohortInclusionResultEntity();
            cohortInclusionResultEntity.setCohortDefinitionId((long)id);
            cohortInclusionResultEntity.setInclusionRuleMask(cohortInclusionResult.getInclusionRuleMask());
            cohortInclusionResultEntity.setPersonCount(cohortInclusionResult.getPersonCount());
            cohortInclusionResultEntities.add(cohortInclusionResultEntity);
        }
        cohortInclusionResultRepository.save(cohortInclusionResultEntities);

        List<CohortInclusionStatsEntity> cohortInclusionStatsList = new ArrayList<>();
        for(CohortInclusionStatsEntity cohortInclusionStats: cohortGenerationResults.cohortInclusionStats){
            CohortInclusionStatsEntity cohortInclusionStatsEntity = new CohortInclusionStatsEntity();
            cohortInclusionStatsEntity.setCohortDefinitionId((long)id);
            cohortInclusionStatsEntity.setGainCount(cohortInclusionStats.getGainCount());
            cohortInclusionStatsEntity.setPersonCount(cohortInclusionStats.getPersonCount());
            cohortInclusionStatsEntity.setPersonTotal(cohortInclusionStats.getPersonTotal());
            cohortInclusionStatsEntity.setRuleSequence(cohortInclusionStats.getRuleSequence());
            cohortInclusionStatsList.add(cohortInclusionStatsEntity);
        }
        cohortInclusionStatsRepository.save(cohortInclusionStatsList);

        List<CohortSummaryStatsEntity> cohortSummaryStatsList = new ArrayList<>();
        for(CohortSummaryStatsEntity cohortSummaryStats: cohortGenerationResults.cohortSummaryStats){
            CohortSummaryStatsEntity cohortSummaryStatsEntity = new CohortSummaryStatsEntity();
            cohortSummaryStatsEntity.setCohortDefinitionId((long)id);
            cohortSummaryStatsEntity.setBaseCount(cohortSummaryStats.getBaseCount());
            cohortSummaryStatsEntity.setFinalCount(cohortSummaryStats.getFinalCount());
            cohortSummaryStatsList.add(cohortSummaryStatsEntity);
        }
        cohortSummaryStatsRepository.save(cohortSummaryStatsList);

        return cohortGenerationResults;
    }

    @Transactional
    List<CohortGenerationInfo> importCohortGenerationInfo(int id, String sourceKey, List<CohortGenerationInfo> cohortGenerationInfo) {
        List<CohortGenerationInfo> cohortGenerationInfoList = new ArrayList<>();
        for(CohortGenerationInfo cgi : cohortGenerationInfo){
            CohortGenerationInfo cohortGenerationInfoAdapted = new CohortGenerationInfo(this.cohortDefinitionRepository.findOne(id),cohortDefinitionService.getSourceRepository().findBySourceKey(sourceKey).getSourceId());
            cohortGenerationInfoAdapted.setStatus(cgi.getStatus());
            cohortGenerationInfoAdapted.setExecutionDuration(cgi.getExecutionDuration());
            cohortGenerationInfoAdapted.setIsValid(cgi.isIsValid());
            cohortGenerationInfoAdapted.setStartTime(cgi.getStartTime());
            cohortGenerationInfoAdapted.setFailMessage(cgi.getFailMessage());
            cohortGenerationInfoAdapted.setPersonCount(cgi.getPersonCount());
            cohortGenerationInfoAdapted.setRecordCount(cgi.getRecordCount());
            cohortGenerationInfoAdapted.setIncludeFeatures(cgi.isIncludeFeatures());
            cohortGenerationInfoList.add(cohortGenerationInfoAdapted);
        }
        cohortGenerationInfoRepository.save(cohortGenerationInfoList);
        return cohortGenerationInfoList;
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

    private Response getResponse(File file) {
        ByteArrayOutputStream exportStream = null;
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            exportStream = new ByteArrayOutputStream(bytes.length);
            exportStream.write(bytes, 0, bytes.length);
        } catch (IOException e){
            log.error(e.getMessage(), e);
        }

        return Response
                .ok(exportStream)
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()))
                .build();
    }




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
                .filter(roleEntity -> roleEntity != null)
                .collect(Collectors.toList());

        List<RoleEntity> existingOrganizationRolesPermissionRemove = organizations.stream()
                .map(organisation -> StreamSupport.stream(roles.spliterator(), false)
                        .filter(roleEntity -> roleEntity.getName().equals(organisation.getName()) && !organisation.isCanRead())
                        .findFirst().orElse(null))
                .filter(roleEntity -> roleEntity != null)
                .collect(Collectors.toList());

        List<Organization> organisationsWithoutRoles = organizations.stream()
                .filter(organisation -> organisation.isCanRead() && StreamSupport.stream(roles.spliterator(), false)
                        .filter(roleEntity -> roleEntity.getName().equals(organisation.getName()))
                        .collect(Collectors.toList()).size() == 0)
                .collect(Collectors.toList());

        PermissionEntity permissionEntity = this.authorizer.getPermissionByValue("cohortdefinition:" + newDef.getId() + ":get");

        for (Organization org: organisationsWithoutRoles) {
            RoleEntity roleEntity = this.authorizer.addOrganizationRole(org.getName());
            if(roleEntity != null){
                existingOrganizationRolesPermissionAdd.add(roleEntity);
            }
        }

        for (RoleEntity role: existingOrganizationRolesPermissionAdd) {
            if(permissionEntity != null){
                this.authorizer.addPermission(role, permissionEntity);
            }
        }

        for(RoleEntity role: existingOrganizationRolesPermissionRemove) {
            if(permissionEntity != null) {
                this.authorizer.removePermission(permissionEntity.getId(), role.getId());
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
