package com.jnj.honeur.webapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jnj.honeur.security.SecurityUtils2;
import com.jnj.honeur.webapi.SourceDaimonContextHolder;
import com.jnj.honeur.webapi.cohortdefinition.CohortGenerationInfoRepository;
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
import com.jnj.honeur.webapi.shiro.HoneurTokenManager;
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
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.RoleRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
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

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${security.enabled}")
    private boolean securityEnabled;


    /**
     * Returns all cohort definitions in amazon to which the user has access
     *
     * @return List of cohort_definition
     */
    @GET
    @Path("/hss/list/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CohortDefinitionStorageInformationItem> getCohortDefinitionImportList(@HeaderParam("Authorization") String token) {
        return storageServiceClient.getCohortDefinitionImportList(token);
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
    public List<StorageInformationItem> getCohortDefinitionResultsImportList(@HeaderParam("Authorization") String token, @PathParam("id") final int id) {
        if (cohortDefinitionRepository.findOne(id) == null || cohortDefinitionRepository.findOne(id).getUuid() == null) {
            throw new IllegalArgumentException(String.format("Definition with ID=%s does not exist or is not yet shared!", id));
        }
        return storageServiceClient.getCohortDefinitionResultsImportList(token, cohortDefinitionRepository.findOne(id).getUuid());
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
    public CohortDefinitionService.CohortDefinitionDTO createCohortDefinitionFromFile(@HeaderParam("Authorization") String token, final StorageInformationItem storageInformationItem) {
        String expression = storageServiceClient.getCohortDefinition(token, storageInformationItem.getUuid());

        CohortDefinitionService.CohortDefinitionDTO def = new CohortDefinitionService.CohortDefinitionDTO();
        def.expression = expression;
        def.name = storageInformationItem.getOriginalFilename().replace(".cohort","");
        def.expressionType = ExpressionType.SIMPLE_EXPRESSION;
        def.organizations = new ArrayList<>();
        def.uuid = UUID.fromString(storageInformationItem.getUuid());

        addGenerationPermissions(def);

        return cohortDefinitionService.createCohortDefinition(def);
    }

    private void addGenerationPermissions(CohortDefinitionService.CohortDefinitionDTO createdDefinition) {
        if(securityEnabled) {
            //TODO make more central (code duplication in HoneurCohortService.java
            Collection<SourceInfo> sources = sourceService.getSources();
            for (SourceInfo sourceInfo : sources) {
                HashMap<String, String> map = new HashMap<>();
                map.put("cohortdefinition:%s:generate:" + sourceInfo.sourceKey + ":get",
                        "Generate Cohort Definition generation results for defintion with ID = %s for source " + sourceInfo.sourceKey);
                map.put("cohortdefinition:%s:export:" + sourceInfo.sourceKey + ":get",
                        "Export Cohort Definition generation results for defintion with ID = %s for source " + sourceInfo.sourceKey);
                map.put("cohortdefinition:%s:report:" + sourceInfo.sourceKey + ":get",
                        "View Cohort Definition generation results for defintion with ID = %s for source " + sourceInfo.sourceKey);
                List<SourceDaimon> daimonsForGeneration = sourceInfo.daimons.stream()
                        .filter(sourceDaimon -> sourceDaimon.getDaimonType().equals(SourceDaimon.DaimonType.CDM) ||
                                sourceDaimon.getDaimonType().equals(SourceDaimon.DaimonType.Vocabulary) ||
                                sourceDaimon.getDaimonType().equals(SourceDaimon.DaimonType.Results))
                        .collect(Collectors.toList());
                if (daimonsForGeneration.size() == 3) {
                    try {
                        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
                        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, map,
                                String.valueOf(createdDefinition.id));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
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
    public CohortGenerationResults createCohortDefinitionResultsFromFile(@HeaderParam("Authorization") String token, @PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, final StorageInformationItem storageInformationItem) throws Exception {
        CohortGenerationResults results = storageServiceClient.getCohortGenerationResults(token,
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
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{id}/export/{sourceKey}")
    public Response exportCohortResults(@HeaderParam("Authorization") String token, @PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, @QueryParam("toCloud") final boolean toCloud, HttpHeaders headers) {
        try {
            CohortGenerationInfo info = this.cohortGenerationInfoRepository.findGenerationInfoByIdAndSourceId(id, sourceRepository.findBySourceKey(sourceKey).getSourceId());

            SourceDaimonContextHolder
                    .setCurrentSourceDaimonContext(new SourceDaimonContext(sourceKey, SourceDaimon.DaimonType.Results));

            List<CohortEntity> cohorts = cohortRepository.findByCohortDefinitionId((long) id);
            List<CohortInclusionEntity> cohortInclusions =
                    cohortInclusionRepository.findByCohortDefinitionId((long) id);
            List<CohortInclusionResultEntity> cohortInclusionResults =
                    cohortInclusionResultRepository.findByCohortDefinitionId((long) id);
            List<CohortInclusionStatsEntity> cohortInclusionStats =
                    cohortInclusionStatsRepository.findByCohortDefinitionId((long) id);
            List<CohortSummaryStatsEntity> cohortSummaryStats =
                    cohortSummaryStatsRepository.findByCohortDefinitionId((long) id);

            CohortGenerationResults results = new CohortGenerationResults();
            results.setCohort(cohorts);
            results.setCohortInclusion(cohortInclusions);
            results.setCohortInclusionResult(cohortInclusionResults);
            results.setCohortInclusionStats(cohortInclusionStats);
            results.setCohortSummaryStats(cohortSummaryStats);

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

            CohortDefinition cohortDefinition = this.cohortDefinitionRepository.findOneWithDetail(id);

            if(toCloud && file != null){
                storageServiceClient.saveResults(token, file,cohortDefinition.getUuid().toString());
            }
            return getResponse(file);
        } catch(Exception e){
            log.error(e.getMessage(), e);
            // TODO: beter return
            return null;
        } finally {
            SourceDaimonContextHolder.clear();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/export")
    public CohortDefinitionService.CohortDefinitionDTO exportCohortDefinition(@HeaderParam("Authorization") String token, @Context HttpServletRequest request, @PathParam("id") final int id, @QueryParam("toCloud") final boolean toCloud) {
        CohortDefinition cohortDefinition = this.cohortDefinitionRepository.findOneWithDetail(id);

        String expression = cohortDefinition.getDetails().getExpression();

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File file = createFile(cohortDefinition.getName()+"-"+timeStamp+".cohort", expression);
        if(toCloud && file != null){
            String uuid;
            if(cohortDefinition.getGroupKey() == null){
                UUID groupKey = UUID.randomUUID();
                uuid = storageServiceClient.saveCohort(token, file, groupKey);
                cohortDefinition.setGroupKey(groupKey);
            } else {
                uuid = storageServiceClient.saveCohort(token, file, cohortDefinition.getGroupKey());
            }
            cohortDefinition.setUuid(UUID.fromString(uuid));
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

        addGenerationPermissions(copyDef);

        return copyDef;
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
                        .map(Organization::getName)
                        .collect(Collectors.toList())
                        .contains(roleEntity.getName()))
                .map(RoleEntity::getName)
                .collect(Collectors.toList());

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
    public List<String> getUUIDSList(@HeaderParam("token") String token){
        log.info("Path: /cohortdefinition/uuids");
        String permissionPattern = "cohortdefinition:([0-9]+|\\*):get";
        List<Integer> definitionIds = this.authorizer.getUserPermissions(SecurityUtils2.getSubject(token)).stream()
                .map(PermissionEntity::getValue)
                .filter(permissionString -> permissionString.matches(permissionPattern))
                .map(permissionString -> parseCohortDefinitionId(permissionString))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        log.info(String.format("User has access to : %s", definitionIds));

        List<String> uuids = new ArrayList<>();
        if(definitionIds.size() > 0) {
            List<CohortDefinition> cohortDefinitions = cohortDefinitionRepository.findFromList(definitionIds);
            for (CohortDefinition def : cohortDefinitions) {
                if (def.getUuid() != null) {
                    uuids.add(def.getUuid().toString());
                }
            }
        }
        return uuids;
    }

    private Optional<Integer> parseCohortDefinitionId(String permission) {
        try {
            return Optional.of(Integer.parseInt(permission.split(":")[1]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
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
    public CohortGenerationResults importCohortResults(@HeaderParam("Authorization") String token, @PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, CohortGenerationResults cohortGenerationResults){
        try {
            SourceDaimonContextHolder.setCurrentSourceDaimonContext(new SourceDaimonContext(sourceKey, SourceDaimon.DaimonType.Results));
            CohortGenerationResults results = importCohortGenerationResults(id, cohortGenerationResults);
            SourceDaimonContextHolder.clear();

            results.setCohortGenerationInfo(
                    importCohortGenerationInfo(id, sourceKey, cohortGenerationResults.getCohortGenerationInfo()));

            addViewPermissions(token, id, sourceKey);

            return results;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // TODO: better return
            return new CohortGenerationResults();
        } finally {
            SourceDaimonContextHolder.clear();
        }
    }

    private void addViewPermissions(String token, int id, String sourceKey) {
        //TODO make more central (code duplication in HoneurCohortService.java
            HashMap<String, String> map = new HashMap<>();
            map.put("cohortdefinition:%s:report:"+sourceKey+":get", "View Cohort Definition generation results for defintion with ID = %s for source "+sourceKey);

            try {
                RoleEntity currentUserPersonalRole = roleRepository.findByName(HoneurTokenManager.getSubject(token.replace("Bearer ", "")));
                authorizer.addPermissionsFromTemplate(currentUserPersonalRole, map,
                        String.valueOf(id));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
    }

    @Transactional
    CohortGenerationResults importCohortGenerationResults(int id, CohortGenerationResults cohortGenerationResults) {
        CohortGenerationResults newResults = new CohortGenerationResults();

        List<CohortEntity> cohortEntities = new ArrayList<>();
        for(CohortEntity cohort: cohortGenerationResults.getCohort()){
            CohortEntity cohortEntity = new CohortEntity();
            cohortEntity.setCohortDefinitionId((long)id);
            cohortEntity.setCohortEndDate(cohort.getCohortEndDate());
            cohortEntity.setCohortStartDate(cohort.getCohortStartDate());
            cohortEntity.setSubjectId(cohort.getSubjectId());
            cohortEntities.add(cohortEntity);
        }
        newResults.setCohort(Lists.newArrayList(cohortRepository.save(cohortEntities)));

        List<CohortInclusionEntity> cohortInclusionEntities = new ArrayList<>();
        for(CohortInclusionEntity cohortInclusion: cohortGenerationResults.getCohortInclusion()){
            CohortInclusionEntity cohortInclusionEntity = new CohortInclusionEntity();
            cohortInclusionEntity.setCohortDefinitionId((long) id);
            cohortInclusionEntity.setDescription(cohortInclusion.getDescription());
            cohortInclusionEntity.setName(cohortInclusion.getName());
            cohortInclusionEntity.setRuleSequence(cohortInclusion.getRuleSequence());
            cohortInclusionEntities.add(cohortInclusionEntity);
        }
        newResults.setCohortInclusion(Lists.newArrayList(cohortInclusionRepository.save(cohortInclusionEntities)));

        List<CohortInclusionResultEntity> cohortInclusionResultEntities = new ArrayList<>();
        for(CohortInclusionResultEntity cohortInclusionResult: cohortGenerationResults.getCohortInclusionResult()){
            CohortInclusionResultEntity cohortInclusionResultEntity = new CohortInclusionResultEntity();
            cohortInclusionResultEntity.setCohortDefinitionId((long)id);
            cohortInclusionResultEntity.setInclusionRuleMask(cohortInclusionResult.getInclusionRuleMask());
            cohortInclusionResultEntity.setPersonCount(cohortInclusionResult.getPersonCount());
            cohortInclusionResultEntities.add(cohortInclusionResultEntity);
        }
        newResults.setCohortInclusionResult(
                Lists.newArrayList(cohortInclusionResultRepository.save(cohortInclusionResultEntities)));

        List<CohortInclusionStatsEntity> cohortInclusionStatsList = new ArrayList<>();
        for(CohortInclusionStatsEntity cohortInclusionStats: cohortGenerationResults.getCohortInclusionStats()){
            CohortInclusionStatsEntity cohortInclusionStatsEntity = new CohortInclusionStatsEntity();
            cohortInclusionStatsEntity.setCohortDefinitionId((long)id);
            cohortInclusionStatsEntity.setGainCount(cohortInclusionStats.getGainCount());
            cohortInclusionStatsEntity.setPersonCount(cohortInclusionStats.getPersonCount());
            cohortInclusionStatsEntity.setPersonTotal(cohortInclusionStats.getPersonTotal());
            cohortInclusionStatsEntity.setRuleSequence(cohortInclusionStats.getRuleSequence());
            cohortInclusionStatsList.add(cohortInclusionStatsEntity);
        }
        newResults.setCohortInclusionStats(
                Lists.newArrayList(cohortInclusionStatsRepository.save(cohortInclusionStatsList)));

        List<CohortSummaryStatsEntity> cohortSummaryStatsList = new ArrayList<>();
        for(CohortSummaryStatsEntity cohortSummaryStats: cohortGenerationResults.getCohortSummaryStats()){
            CohortSummaryStatsEntity cohortSummaryStatsEntity = new CohortSummaryStatsEntity();
            cohortSummaryStatsEntity.setCohortDefinitionId((long)id);
            cohortSummaryStatsEntity.setBaseCount(cohortSummaryStats.getBaseCount());
            cohortSummaryStatsEntity.setFinalCount(cohortSummaryStats.getFinalCount());
            cohortSummaryStatsList.add(cohortSummaryStatsEntity);
        }
        newResults.setCohortSummaryStats(Lists.newArrayList(cohortSummaryStatsRepository.save(cohortSummaryStatsList)));

        List<CohortFeaturesEntity> cohortFeaturesEntities = new ArrayList<>();
        for(CohortFeaturesEntity cohortFeatures: cohortGenerationResults.getCohortFeatures()){
            CohortFeaturesEntity cohortFeaturesEntity = new CohortFeaturesEntity();
            cohortFeaturesEntity.setCohortDefinitionId((long)id);
            cohortFeaturesEntity.setAverageValue(cohortFeatures.getAverageValue());
            cohortFeaturesEntity.setCovariateId(cohortFeatures.getCovariateId());
            cohortFeaturesEntity.setSumValue(cohortFeatures.getSumValue());
            cohortFeaturesEntities.add(cohortFeaturesEntity);
        }
        newResults.setCohortFeatures(Lists.newArrayList(cohortFeaturesRepository.save(cohortFeaturesEntities)));

        List<CohortFeaturesAnalysisRefEntity> cohortFeaturesAnalysisRefEntities = new ArrayList<>();
        for(CohortFeaturesAnalysisRefEntity cohortFeaturesAnalysisRef: cohortGenerationResults.getCohortFeaturesAnalysisRef()){
            CohortFeaturesAnalysisRefEntity cohortFeaturesAnalysisRefEntity = new CohortFeaturesAnalysisRefEntity();
            cohortFeaturesAnalysisRefEntity.setCohortDefinitionId((long) id);
            cohortFeaturesAnalysisRefEntity.setAnalysisId(cohortFeaturesAnalysisRef.getAnalysisId());
            cohortFeaturesAnalysisRefEntity.setAnalysisName(cohortFeaturesAnalysisRef.getAnalysisName());
            cohortFeaturesAnalysisRefEntity.setBinary(cohortFeaturesAnalysisRef.getBinary());
            cohortFeaturesAnalysisRefEntity.setDomainId(cohortFeaturesAnalysisRef.getDomainId());
            cohortFeaturesAnalysisRefEntity.setEndDay(cohortFeaturesAnalysisRef.getEndDay());
            cohortFeaturesAnalysisRefEntity.setMissingMeansZero(cohortFeaturesAnalysisRef.getMissingMeansZero());
            cohortFeaturesAnalysisRefEntity.setStartDay(cohortFeaturesAnalysisRef.getStartDay());
            cohortFeaturesAnalysisRefEntities.add(cohortFeaturesAnalysisRefEntity);
        }
        newResults.setCohortFeaturesAnalysisRef(Lists.newArrayList(cohortFeaturesAnalysisRefRepository.save(cohortFeaturesAnalysisRefEntities)));

        List<CohortFeaturesDistEntity> cohortFeaturesDistEntities = new ArrayList<>();
        for(CohortFeaturesDistEntity cohortFeaturesDist: cohortGenerationResults.getCohortFeaturesDist()){
            CohortFeaturesDistEntity cohortFeaturesDistEntity = new CohortFeaturesDistEntity();
            cohortFeaturesDistEntity.setCohortDefinitionId((long) id);
            cohortFeaturesDistEntity.setCovariateId(cohortFeaturesDist.getCovariateId());
            cohortFeaturesDistEntity.setCountValue(cohortFeaturesDist.getCountValue());
            cohortFeaturesDistEntity.setMinValue(cohortFeaturesDist.getMinValue());
            cohortFeaturesDistEntity.setMaxValue(cohortFeaturesDist.getMaxValue());
            cohortFeaturesDistEntity.setAverageValue(cohortFeaturesDist.getAverageValue());
            cohortFeaturesDistEntity.setStandardDeviation(cohortFeaturesDist.getStandardDeviation());
            cohortFeaturesDistEntity.setMedianValue(cohortFeaturesDist.getMedianValue());
            cohortFeaturesDistEntity.setP10Value(cohortFeaturesDist.getP10Value());
            cohortFeaturesDistEntity.setP25Value(cohortFeaturesDist.getP25Value());
            cohortFeaturesDistEntity.setP75Value(cohortFeaturesDist.getP75Value());
            cohortFeaturesDistEntity.setP90Value(cohortFeaturesDist.getP90Value());
            cohortFeaturesDistEntities.add(cohortFeaturesDistEntity);
        }
        newResults.setCohortFeaturesDist(Lists.newArrayList(cohortFeaturesDistRepository.save(cohortFeaturesDistEntities)));

        List<CohortFeaturesRefEntity> cohortFeaturesRefEntities = new ArrayList<>();
        for(CohortFeaturesRefEntity cohortFeaturesRef: cohortGenerationResults.getCohortFeaturesRef()){
            CohortFeaturesRefEntity cohortFeaturesRefEntity = new CohortFeaturesRefEntity();
            cohortFeaturesRefEntity.setCohortDefinitionId((long) id);
            cohortFeaturesRefEntity.setCovariateId(cohortFeaturesRef.getCovariateId());
            cohortFeaturesRefEntity.setCovariateName(cohortFeaturesRef.getCovariateName());
            cohortFeaturesRefEntity.setAnalysisId(cohortFeaturesRef.getAnalysisId());
            cohortFeaturesRefEntity.setConceptId(cohortFeaturesRef.getConceptId());
            cohortFeaturesRefEntities.add(cohortFeaturesRefEntity);
        }
        newResults.setCohortFeaturesRef(Lists.newArrayList(cohortFeaturesRefRepository.save(cohortFeaturesRefEntities)));

        return newResults;
    }

    @Transactional
    CohortGenerationInfo importCohortGenerationInfo(int id, String sourceKey, CohortGenerationInfo cohortGenerationInfo) {

        CohortGenerationInfo cohortGenerationInfoAdapted = new CohortGenerationInfo(this.cohortDefinitionRepository.findOne(id),cohortDefinitionService.getSourceRepository().findBySourceKey(sourceKey).getSourceId());
        cohortGenerationInfoAdapted.setStatus(cohortGenerationInfo.getStatus());
        cohortGenerationInfoAdapted.setExecutionDuration(cohortGenerationInfo.getExecutionDuration());
        cohortGenerationInfoAdapted.setIsValid(cohortGenerationInfo.isIsValid());
        cohortGenerationInfoAdapted.setStartTime(cohortGenerationInfo.getStartTime());
        cohortGenerationInfoAdapted.setFailMessage(cohortGenerationInfo.getFailMessage());
        cohortGenerationInfoAdapted.setPersonCount(cohortGenerationInfo.getPersonCount());
        cohortGenerationInfoAdapted.setRecordCount(cohortGenerationInfo.getRecordCount());
        cohortGenerationInfoAdapted.setIncludeFeatures(cohortGenerationInfo.isIncludeFeatures());

        return cohortGenerationInfoRepository.save(cohortGenerationInfoAdapted);
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
