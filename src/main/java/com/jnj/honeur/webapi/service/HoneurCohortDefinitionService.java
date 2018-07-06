/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jnj.honeur.webapi.service;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Customization of CohortDefinitionService of OHDSI
 * @author Sander Bylemans
 */
@Component("honeurCohortDefinitionService")
@ConditionalOnProperty(value = "datasource.honeur.enabled", havingValue = "true")
public class HoneurCohortDefinitionService extends CohortDefinitionService {

    @Autowired
    private Security security;

    @Autowired
    protected PermissionManager authorizer;

    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;

    @Autowired
    private SourceService sourceService;

    @Value("${security.enabled}")
    private boolean securityEnabled;



    /**
     * Returns all most recent cohort definitions in the cohort schema, the ones who are not a previous version to another.
     *
     * @return List of cohort_definition
     */
    @Override
    public List<CohortDefinitionListItem> getCohortDefinitionList() {
        ArrayList<CohortDefinitionListItem> result = new ArrayList<>();
        Iterable<CohortDefinition> defs = cohortDefinitionRepository.findAll();

        List<UUID> previousVersionsUuids = StreamSupport.stream(defs.spliterator(), false)
                .filter(cohortDefinition -> cohortDefinition.getPreviousVersion() != null)
                .filter(cohortDefinition -> cohortDefinition.getPreviousVersion().getUuid() != null)
                .map(CohortDefinition::getPreviousVersion)
                .map(CohortDefinition::getUuid)
                .collect(Collectors.toList());

        List<CohortDefinition> filteredDefs = StreamSupport.stream(defs.spliterator(), false)
                .filter(cohortDefinition -> !previousVersionsUuids.contains(cohortDefinition.getUuid()))
                .collect(Collectors.toList());

        for (CohortDefinition d : filteredDefs) {
            CohortDefinitionDTO item = new CohortDefinitionDTO();
            item.id = d.getId();
            item.name = d.getName();
            item.description = d.getDescription();
            item.expressionType = d.getExpressionType();
            item.createdBy = d.getCreatedBy();
            item.createdDate = d.getCreatedDate();
            item.modifiedBy = d.getModifiedBy();
            item.modifiedDate = d.getModifiedDate();

            item.uuid = d.getUuid();
            item.groupKey = d.getGroupKey();
            List<CohortDefinitionDTO> groupedPreviousVersions =
                    StreamSupport.stream(defs.spliterator(), false)
                            .filter(cohortDefinition -> (cohortDefinition.getGroupKey() == null || cohortDefinition.getGroupKey().equals(item.groupKey)) && !cohortDefinition.getId().equals(item.id))
                            .sorted(Comparator.comparing(CohortDefinition::getCreatedDate).reversed())
                            .map(this::cohortDefinitionToDTO).collect(Collectors.toList());

            if(groupedPreviousVersions.size() > 0) {

                item.previousVersion = groupedPreviousVersions.get(0);

                CohortDefinitionDTO previous = groupedPreviousVersions.get(0);
                for (int i = 1; i < groupedPreviousVersions.size(); i++) {
                    previous.previousVersion = groupedPreviousVersions.get(i);

                    previous = groupedPreviousVersions.get(i);
                }

                result.add(item);
            }
        }
        return result;
    }

    /**
     * Creates the cohort definition, and connects the organizations that have access to this definition
     *
     * @param def The cohort definition to create.
     * @return The new CohortDefinition
     */
    @Override
    public CohortDefinitionDTO createCohortDefinition(CohortDefinitionDTO def) {
        Date currentTime = Calendar.getInstance().getTime();

        //create definition in 2 saves, first to get the generated ID for the new def
        // then to associate the details with the definition
        CohortDefinition newDef = new CohortDefinition();
        newDef.setName(def.name)
                .setDescription(def.description)
                .setCreatedBy(security.getSubject())
                .setCreatedDate(currentTime)
                .setExpressionType(def.expressionType)
                .setPreviousVersion(def.previousVersion == null ? null : cohortDefinitionRepository.findByUuid(def.previousVersion.uuid))
                .setUuid(def.uuid)
                .setGroupKey(def.groupKey);

        newDef = this.cohortDefinitionRepository.save(newDef);

        // associate details
        CohortDefinitionDetails details = new CohortDefinitionDetails();
        details.setCohortDefinition(newDef)
                .setExpression(def.expression);

        newDef.setDetails(details);

        CohortDefinition createdDefinition = this.cohortDefinitionRepository.save(newDef);

        // Add generation permission if the source daimons are there
        addGenerationPermissions(createdDefinition);

        return cohortDefinitionToDTO(createdDefinition);
    }

    private void addGenerationPermissions(CohortDefinition createdDefinition) {
        if(securityEnabled) {
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
                                String.valueOf(createdDefinition.getId()));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Saves the cohort definition for the given id
     *
     * @param id The cohort definition id
     * @return The CohortDefinition
     */
    @Override
    public CohortDefinitionDTO saveCohortDefinition(final int id, CohortDefinitionDTO def) {
        Date currentTime = Calendar.getInstance().getTime();

        CohortDefinition currentDefinition = this.cohortDefinitionRepository.findOneWithDetail(id);

        currentDefinition.setName(def.name)
                .setDescription(def.description)
                .setExpressionType(def.expressionType)
                .setModifiedBy(security.getSubject())
                .setModifiedDate(currentTime)
                .getDetails().setExpression(def.expression);

        this.cohortDefinitionRepository.save(currentDefinition);

        return getCohortDefinition(id);
    }

    @PostConstruct
    public void initIt() throws Exception {
        System.out.println("HONEUR COHORT DEFINITION SERVICE CREATED");
    }
}
