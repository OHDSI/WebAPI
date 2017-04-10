/*
 * Copyright 2017 Observational Health Data Sciences and Informatics [OHDSI.org].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.study.Study;
import org.ohdsi.webapi.study.StudyCohort;
import org.ohdsi.webapi.study.StudySource;
import org.ohdsi.webapi.study.report.Report;
import org.ohdsi.webapi.study.report.ReportCohortPair;
import org.ohdsi.webapi.study.report.ReportContent;
import org.ohdsi.webapi.study.report.ReportCovariate;
import org.ohdsi.webapi.study.report.ReportRepository;
import org.ohdsi.webapi.study.report.ReportSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Path("/report")
@Component
public class StudyReportService extends AbstractDaoService  {

  @Autowired
  StudyService studyService;

  @Autowired
  ReportRepository reportRepository;

  @Autowired
  private Security security;

  @PersistenceContext
  protected EntityManager entityManager;
  
  public static class ReportListItem {
    public Integer id;
    public String name;
    public String description;
    public Integer studyId;
    public String studyName;
  }
  
  public static class ReportDTO extends ReportListItem {
    public List<StudyService.CohortDetail> cohorts;
    public List<CohortPair> cohortPairs;
    public Set<ReportCovariate> covariates;
    public List<ReportContent> content;
    public List<ReportSourceDTO> sources;
  }

  public static class CohortPair {
    public int target;
    public int outcome;
    public boolean isActive;
    
    public CohortPair() {}
            
    public CohortPair(int target, int outcome, boolean isActive) {
      this.target = target;
      this.outcome = outcome;
      this.isActive = isActive;
    }
  }
  
  public static class ReportSourceDTO extends StudyService.StudySourceDTO {
   public boolean isActive;
   
   public ReportSourceDTO() {}
   
   public ReportSourceDTO (int sourceId, String name, boolean isActive) {
     this.sourceId = sourceId;
     this.name = name;
     this.isActive = isActive;
   }
  }
  
  private ReportDTO fromReport(Report report) {
    HashMap<Integer, StudyService.CohortDetail> cohorts = new HashMap<>();

    ReportDTO result = new ReportDTO();
    result.id = report.getId();
    result.name = report.getName();
    result.description = report.getDescription();
    result.studyId = report.getStudy().getId();
    result.studyName = report.getStudy().getName();
    
    result.cohortPairs = report.getCohortPairs().stream().map(p -> {
      StudyService.CohortDetail target;
      if ((target = cohorts.get(p.getTarget().getId())) == null) {
        target = studyService.fromStudyCohort(p.getTarget());
        cohorts.put(target.cohortId, target);
      }

      StudyService.CohortDetail outcome;
      if ((outcome = cohorts.get(p.getOutcome().getId())) == null) {
        outcome = studyService.fromStudyCohort(p.getOutcome());
        cohorts.put(outcome.cohortId, outcome);
      }
      
      cohorts.put(target.cohortId, target);
      cohorts.put(outcome.cohortId, outcome);
      CohortPair pair = new CohortPair(target.cohortId, outcome.cohortId, p.isActive());
      return pair;
    }).collect(Collectors.toList());
    result.covariates = report.getCovariates().stream().collect(Collectors.toSet());
    result.cohorts = cohorts.values().stream().collect(Collectors.toList());
    result.content = report.getContent().stream().collect(Collectors.toList());
    result.sources = report.getSources().stream().map(s -> {
      return new ReportSourceDTO(s.getSource().getId(), s.getSource().getName(), s.isActive()); 
    }).collect(Collectors.toList());
    
    return result;    
  }
  
  private ReportDTO save(ReportDTO report)
  {
    Date currentTime = Calendar.getInstance().getTime();
    Report reportEntity;
    
    if (report.id != null) {
      reportEntity = reportRepository.findOne(report.id);
      reportEntity.setModifiedDate(currentTime);
      reportEntity.setModifiedBy(security.getSubject());
    }
    else {
      reportEntity = new Report();
      reportEntity.setCreatedDate(currentTime);
      reportEntity.setCreatedBy(security.getSubject());
      reportEntity.setModifiedBy(null);
      reportEntity.setModifiedDate(null);
      reportEntity.setStudy(entityManager.getReference(Study.class,  report.studyId));
    }
    
    reportEntity.setName(report.name);
    reportEntity.setDescription(report.description);
    reportEntity.setCohortPairs(report.cohortPairs.stream().map(p -> {
      ReportCohortPair pair = new ReportCohortPair();
      pair.setTarget(entityManager.getReference(StudyCohort.class, p.target));
      pair.setOutcome(entityManager.getReference(StudyCohort.class, p.outcome));
      pair.setActive(p.isActive);
      return pair;
    }).collect(Collectors.toList()));
    reportEntity.setContent(report.content);
    reportEntity.setCovariates(report.covariates);
    reportEntity.setSources(report.sources.stream().map(s -> {
      ReportSource source = new ReportSource();
      source.setActive(s.isActive);
      source.setSource(entityManager.getReference(StudySource.class, s.sourceId));
      return source;
    }).collect(Collectors.toList()));
    
    reportEntity = reportRepository.save(reportEntity);
    
    return fromReport(reportEntity);
  }

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ReportListItem> getReportList() {
    List<Report> reports = reportRepository.list();
    
    List<ReportListItem> result = reports.stream().map(r -> {
      ReportListItem item = new ReportListItem();
      item.id = r.getId();
      item.name = r.getName();
      item.description = r.getDescription();
      item.studyId = r.getStudy().getId();
      item.studyName = r.getStudy().getName();
      return item;
    }).collect(Collectors.toList());
    
    return result;
  }

  @POST
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public ReportDTO createReport(ReportDTO report) {
    if (report.id != null)
    {
      // POST to url should result in a new creation of an entity, so clear any existing reportId.  
      // Alternatively we could throw an exception here.
      report.id = null; 
    }
    
    return save(report);
  }

  @PUT
  @Path("/{reportId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public ReportDTO saveReport(ReportDTO report) {
    return save(report);
  }
  
  @GET
  @Path("/{reportId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public ReportDTO getReport(
          @PathParam("reportId") final int reportId
  ) {
    Report report = reportRepository.findOne(reportId);
    return fromReport(report);
  }
  
}
