/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import org.ohdsi.webapi.cohort.CohortEntity;
import org.ohdsi.webapi.cohort.CohortRepository;
import org.ohdsi.webapi.cohortdefinition.*;
import org.ohdsi.webapi.model.Cohort;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

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
  private CohortDefinitionRepository cohortDefinitionRepository;



  /**
   * Returns all most recent cohort definitions in the cohort schema, the ones who are not a previous version to another.
   *
   * @return List of cohort_definition
   */
  @Override
  public List<CohortDefinitionListItem> getCohortDefinitionList() {
    ArrayList<CohortDefinitionListItem> result = new ArrayList<>();
    Iterable<CohortDefinition> defs = cohortDefinitionRepository.findAll();

    List<UUID> previousVersions = StreamSupport.stream(defs.spliterator(), false)
            .filter(cohortDefinition -> cohortDefinition.getUuid() != null)
            .map(CohortDefinition::getUuid)
            .collect(Collectors.toList());

    List<CohortDefinition> filteredDefs = StreamSupport.stream(defs.spliterator(), false)
            .filter(cohortDefinition -> !previousVersions.contains(cohortDefinition.getUuid()))
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

      result.add(item);
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
            .setPreviousVersion(def.previousVersion == null ? null : cohortDefinitionRepository.findByUuid(def.previousVersion))
            .setUuid(def.uuid)
            .setGroupKey(def.groupKey);

    newDef = this.cohortDefinitionRepository.save(newDef);

    // associate details
    CohortDefinitionDetails details = new CohortDefinitionDetails();
    details.setCohortDefinition(newDef)
            .setExpression(def.expression);

    newDef.setDetails(details);

    CohortDefinition createdDefinition = this.cohortDefinitionRepository.save(newDef);

    return cohortDefinitionToDTO(createdDefinition);
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
