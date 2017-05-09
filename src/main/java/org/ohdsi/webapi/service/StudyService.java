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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.study.Concept;
import org.ohdsi.webapi.study.Study;
import org.ohdsi.webapi.study.StudyCohort;
import org.ohdsi.webapi.study.StudyRepository;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Path("/study")
@Component
public class StudyService extends AbstractDaoService  {

  private final String QUERY_COVARIATE_STATS  = ResourceHelper.GetResourceAsString("/resources/study/sql/queryCovariateStats.sql");
  private final String QUERY_COVARIATE_DIST  = ResourceHelper.GetResourceAsString("/resources/study/sql/queryCovariateDist.sql");
  private final String QUERY_COHORTSETS  = ResourceHelper.GetResourceAsString("/resources/study/sql/queryCohortSets.sql");

  @Autowired
  StudyRepository studyRepository;

  @Autowired
  private Security security;
  
  @PersistenceContext
  protected EntityManager entityManager;
  
  public static class StudyListItem {
    public Integer id;
    public String name;
    public String description;
    
  }
  
  public static class StudyDTO extends StudyListItem {
    public List<CohortDetail> cohorts;
    public List<StudyIRDTO> irAnalysisList;
    public List<StudyCCADTO> ccaList;
    public List<StudySCCDTO> sccList;
    public List<StudySourceDTO> sources;
    
    public StudyDTO () {}
  }
  
  public static class StudyIRDTO {
    
    public Integer id;
    public String params;
    public List<Long> targets;
    public List<Long> outcomes;
    
    public StudyIRDTO() {
    }
    
  }
  
  public static class SCCPair {
    public Long target;
    public Long outcome;
    public List<Long> negativeControls;
  }
  public static class StudySCCDTO {
    public Integer id;
    public String params;
    public List<SCCPair> pairs;
    
    public StudySCCDTO() {}
  }
  
  public static class CCATrio {
    public Long target;
    public Long comaprator;
    public Long outcome;
    public List<Long> negativeControls;
    
    public CCATrio () {}
    
  }

  public static class StudyCCADTO {
    public Integer id;
    public String params;
    public List<CCATrio> trios;
    
    public StudyCCADTO() {}
  }

  public static class StudySourceDTO {
   public int sourceId;
   public String name;
   
   public StudySourceDTO() {}
   
   public StudySourceDTO (int sourceId, String name) {
     this.sourceId = sourceId;
     this.name = name;
   }
  }
    
  public static class CohortRelationship {
    public long target;
    public org.ohdsi.webapi.study.RelationshipType relationshipType;
  }

  public static class CohortDetail {
    public long cohortId;
    public String name;
    public String expression;
    public List<Concept> concepts = new ArrayList<>();
    public List<CohortRelationship> relationships = new ArrayList<>();
  }
  
  
  public static class CovariateStat {
    public long covariateId;
    public String covariateName;
    public long analysisId;
    public String analysisName;
    public String domainId;
    public String timeWindow;
    public long conceptId;
    public long countValue;
    public BigDecimal statValue;
  }

  public static class CovariateDist {
    public long covariateId;
    public String covariateName;
    public long analysisId;
    public String analysisName;
    public String domainId;
    public String timeWindow;
    public long conceptId;
    public long countValue;
    public BigDecimal avgValue;
    public BigDecimal stdevValue;
    public long minValue;
    public long p10Value;
    public long p25Value;
    public long medianValue;
    public long p75Value;
    public long p90Value;
    public long maxValue;
  }
  
  public static class CohortSetListItem {
    public int id;
    public String name;
    public String description;
    public int members;
  }

  public static class StudyStatistics {
    public List<CovariateStat> catagorical = new ArrayList<>();
    public List<CovariateDist> continuous = new ArrayList<>();
  }
  
  private List<String> buildCriteriaClauses (String searchTerm, List<String> analysisIds, List<String> timeWindows) {
    ArrayList<String> clauses = new ArrayList<>();
    
    if (searchTerm != null && searchTerm.length() > 0)
    {
      clauses.add(String.format("lower(ar1.covariate_name) like '%%%s%%'",searchTerm));
    }
    
    if (analysisIds != null && analysisIds.size() > 0)
    {
      ArrayList<String> ids = new ArrayList<>();
      ArrayList<String> ranges = new ArrayList<>();
      
      analysisIds.stream().map((analysisIdExpr) -> analysisIdExpr.split(":")).forEachOrdered((parsedIds) -> {
        if (parsedIds.length > 1) {
          ranges.add(String.format("(ar1.analysis_id >= %s and ar1.analysis_id <= %s)", parsedIds[0], parsedIds[1]));
        } else {
          ids.add(parsedIds[0]);
        }
      });
      
      String idClause = "";
      if (ids.size() > 0)
        idClause = String.format("ar1.analysis_id in (%s)", StringUtils.join(ids, ","));
      
      if (ranges.size() > 0)
        idClause += (idClause.length() > 0 ? " OR " : "") + StringUtils.join(ranges, " OR ");
      
      clauses.add("(" + idClause + ")");
    }
  
    if (timeWindows != null && timeWindows.size() > 0) {
      ArrayList<String> timeWindowClauses = new ArrayList<>();
      timeWindows.forEach((timeWindow) -> {
        timeWindowClauses.add(String.format("ar1.time_window = '%s'", timeWindow));
      });
      clauses.add("(" + StringUtils.join(timeWindowClauses, " OR ") + ")" );
    }
    
    return clauses;
  }
  
  public CohortDetail fromStudyCohort(StudyCohort studyCohort) {
    CohortDetail detail = new CohortDetail();
    detail.cohortId = studyCohort.getId();
    detail.name = studyCohort.getName();
    detail.relationships = studyCohort.getCohortRelationships().stream().map(r -> {
      CohortRelationship rel = new CohortRelationship();
      rel.target = r.getTarget().getId();
      rel.relationshipType = r.getRelationshipType();
      return rel;
    }).collect(Collectors.toList());
    detail.concepts = studyCohort.getConcepts().stream().collect(Collectors.toList());
    return detail;
  }
  
  public StudyDTO fromStudy(Study studyEntity)
  {
    StudyDTO study = new StudyDTO();
    HashMap<Integer, StudyService.CohortDetail> cohorts = new HashMap<>();
    
    study.id = studyEntity.getId();
    study.name = studyEntity.getName();
    study.description = studyEntity.getDescription();
    
    // Map IRAs
    study.irAnalysisList = studyEntity.getIrAnalysisList().stream().map(i -> {
      StudyIRDTO ira = new StudyIRDTO();
      ira.id = i.getId();
      ira.params = i.getParams();
      
      ira.targets = i.getTargets().stream().map(t -> {
        return t.getId();
      }).collect(Collectors.toList());
      
      ira.outcomes = i.getOutcomes().stream().map(o -> {
        return o.getId();
      }).collect(Collectors.toList());

      return ira;
    }).collect(Collectors.toList());
    
    // Map SCCs
    study.sccList = studyEntity.getSccList().stream().map(scc -> {
      StudySCCDTO sccDTO = new StudySCCDTO();
      
      sccDTO.id = scc.getId();
      sccDTO.params = scc.getParams();

      sccDTO.pairs = scc.getPairs().stream().map(pair -> {
        SCCPair pairDTO = new SCCPair();
        
        pairDTO.target = pair.getTarget().getId();
        pairDTO.outcome = pair.getOutcome().getId();
        pairDTO.negativeControls = pair.getNegativeControls().stream().map(StudyCohort::getId).collect(Collectors.toList());
        return pairDTO;
      }).collect(Collectors.toList());
      return sccDTO;
    }).collect(Collectors.toList());
    
    // Map CCAs
    study.ccaList = studyEntity.getCcaList().stream().map(cca -> {
      StudyCCADTO ccaDTO = new StudyCCADTO();
      
      ccaDTO.id = cca.getId();
      ccaDTO.params = cca.getParams();

      ccaDTO.trios = cca.getTrios().stream().map(trio -> {
        CCATrio trioDTO = new CCATrio();
        
        trioDTO.target = trio.getTarget().getId();
        trioDTO.comaprator = trio.getComparator().getId();
        trioDTO.outcome = trio.getOutcome().getId();
        trioDTO.negativeControls = trio.getNegativeControls().stream().map(StudyCohort::getId).collect(Collectors.toList());
        return trioDTO;
      }).collect(Collectors.toList());
      return ccaDTO;
    }).collect(Collectors.toList());

    // Map cohorts
    study.cohorts = studyEntity.getCohortList().stream().map(c -> {
        return fromStudyCohort(c);
    }).collect(Collectors.toList());
    
    // Map Sources
    study.sources = studyEntity.getSourceList().stream().map (s -> {
      return new StudySourceDTO(s.getId(), s.getName());
    }).collect(Collectors.toList());
    
    return study;
  }
  
  
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<StudyListItem> getStudyList(){
    Stream<Study> studyStream = StreamSupport.stream(studyRepository.findAll().spliterator(), true);
    return studyStream.map(s -> {
      StudyListItem item = new StudyListItem();
      item.id = s.getId();
      item.name = s.getName();
      item.description = s.getDescription();
      return item;
    }).collect(Collectors.toList());
  }
          
  @GET
  @Path("{studyId}/results/covariates/{cohortId}/{sourceId}")
  @Produces(MediaType.APPLICATION_JSON)
  public StudyStatistics getStudyStatistics(
          @PathParam("studyId") final int studyId, 
          @PathParam("cohortId") final long cohortId, 
          @PathParam("sourceId") final int sourceId,
          @QueryParam("searchTerm") final String searchTerm,
          @QueryParam("analysisId") final List<String> analysisIds,
          @QueryParam("timeWindow") final List<String> timeWindows
  ) {  
    StudyStatistics result = new StudyStatistics();
    String translatedSql;
    
    List<String> criteriaClauses = buildCriteriaClauses(searchTerm, analysisIds, timeWindows);
    
    String catagoricalQuery = SqlRender.renderSql(
            QUERY_COVARIATE_STATS, 
            new String[] {"study_results_schema", "cohort_definition_id", "source_id", "criteria_clauses"},
            new String[] {this.getStudyResultsSchema(), Long.toString(cohortId), Integer.toString(sourceId), criteriaClauses.isEmpty() ? "" : " AND\n" + StringUtils.join(criteriaClauses, "\n AND ")}
    );
    
    translatedSql = SqlTranslate.translateSql(catagoricalQuery, "sql server", this.getStudyResultsDialect(), SessionUtils.sessionId(), this.getStudyResultsSchema());
    List<CovariateStat> catagoricalStats =  this.getStudyResultsJdbcTemplate().query(translatedSql, (rs,rowNum) -> { 
      CovariateStat mappedRow = new CovariateStat() {{
        covariateId = rs.getLong("covariate_id");
        covariateName = rs.getString("covariate_name");
        analysisId = rs.getLong("analysis_id");
        analysisName = rs.getString("analysis_name");
        domainId = rs.getString("domain_id");
        timeWindow = rs.getString("time_window");
        conceptId = rs.getLong("concept_id");
        countValue = rs.getLong("count_value");
        statValue = new BigDecimal(rs.getDouble("stat_value")).setScale(5, RoundingMode.DOWN);
      }};
      return mappedRow;
    });    
    
    
    String continuousQuery = SqlRender.renderSql(
            QUERY_COVARIATE_DIST, 
            new String[] {"study_results_schema", "cohort_definition_id", "source_id", "criteria_clauses"},
            new String[] {this.getStudyResultsSchema(), Long.toString(cohortId), Integer.toString(sourceId), criteriaClauses.isEmpty() ? "" : " AND\n" +StringUtils.join(criteriaClauses, "\n AND ")}
    );
    
    translatedSql = SqlTranslate.translateSql(continuousQuery, "sql server", this.getStudyResultsDialect(), SessionUtils.sessionId(), this.getStudyResultsSchema());
    List<CovariateDist> continuousStats =  this.getStudyResultsJdbcTemplate().query(translatedSql, (rs,rowNum) -> { 
      CovariateDist mappedRow = new CovariateDist() {{
        covariateId = rs.getLong("covariate_id");
        covariateName = rs.getString("covariate_name");
        analysisId = rs.getLong("analysis_id");
        analysisName = rs.getString("analysis_name");
        domainId = rs.getString("domain_id");
        timeWindow = rs.getString("time_window");
        conceptId = rs.getLong("concept_id");
        countValue = rs.getLong("count_value");
        avgValue = new BigDecimal(rs.getDouble("avg_value")).setScale(5,RoundingMode.DOWN);
        stdevValue = new BigDecimal(rs.getDouble("stdev_value")).setScale(5,RoundingMode.DOWN);
        minValue = rs.getLong("min_value");
        p10Value = rs.getLong("p10_value");
        p25Value = rs.getLong("p25_value");
        medianValue = rs.getLong("median_value");
        p75Value = rs.getLong("p75_value");
        p90Value = rs.getLong("p90_value");
        maxValue = rs.getLong("max_value");
      }};
      return mappedRow;
    });    
    
    result.catagorical = catagoricalStats;
    result.continuous = continuousStats;
    
    return result;
  }
  
  @GET
  @Path("{studyId}/cohortset/search")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CohortSetListItem> getCohortSets(
          @PathParam("studyId") final int studyId,
          @QueryParam("searchTerm") final String searchTerm
  ) {
    String translatedSql;
    
    String cohortSetQuery = SqlRender.renderSql(
            QUERY_COHORTSETS, 
            new String[] {"ohdsi_schema", "study_id", "search_term"},
            new String[] {getOhdsiSchema(), Integer.toString(studyId), searchTerm}
    );
    
    translatedSql = SqlTranslate.translateSql(cohortSetQuery, "sql server", this.getDialect(), SessionUtils.sessionId(), null);
    List<CohortSetListItem> cohortSets =  this.getJdbcTemplate().query(translatedSql, (rs,rowNum) -> { 
      CohortSetListItem mappedRow = new CohortSetListItem() {{
        id =  rs.getInt("id");
        name = rs.getString("name");
        description = rs.getString("description");
        members = rs.getInt("members");
      }};
      
      return mappedRow;
      
    });
    
    return cohortSets; 
  }

  @GET
  @Path("/{studyId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public StudyDTO getStudy(
          @PathParam("studyId") final int studyId
  ) {
    Study studyEntity = studyRepository.findOne(studyId);
    if (studyEntity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
      
    // resolve entity collections into POJO collections for JSON serialization.
    // later we should adopt a DTO mapper when we implement services to update a Study.
    
    return fromStudy(studyEntity);
  }

}
