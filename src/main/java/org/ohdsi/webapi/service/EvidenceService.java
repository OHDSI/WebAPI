package org.ohdsi.webapi.service;

import java.util.Collection;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.evidence.DrugEvidence;
import org.ohdsi.webapi.evidence.EvidenceSummary;
import org.ohdsi.webapi.evidence.EvidenceUniverse;
import org.ohdsi.webapi.evidence.HoiEvidence;
import org.ohdsi.webapi.evidence.DrugHoiEvidence;
import org.ohdsi.webapi.evidence.EvidenceInfo;
import org.ohdsi.webapi.evidence.DrugRollUpEvidence;
import org.ohdsi.webapi.evidence.Evidence;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.stereotype.Component;

/**
 * @author rkboyce based on the vocabulary service written by fdefalco and an
 * initial api written by m_rasteger
 */
@Path("{sourceKey}/evidence/")
@Component
public class EvidenceService extends AbstractDaoService {

  @GET
  @Path("info")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<EvidenceInfo> getInfo(@PathParam("sourceKey") String sourceKey) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getInfo.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"OHDSI_schema"}, new String[]{tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());  // TODO: why is 'sql server' string passed here?

    final List<EvidenceInfo> infoOnSources = new ArrayList<EvidenceInfo>();

    //try {
    List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
        //} catch (EmptyResultDataAccessException e) {
    //    log.debug(String.format("Request for conceptId=%s resulted in 0 results", id));
    //    throw new WebApplicationException(Response.Status.RESET_CONTENT); // http 205
    //}

    for (Map rs : rows) {
      EvidenceInfo info = new EvidenceInfo();
      info.title = (String) rs.get("TITLE");
      info.description = (String) rs.get("DESCRIPTION");
      info.contributer = (String) rs.get("CONTRIBUTER");
      info.creator = (String) rs.get("CREATOR");
      info.creationDate = (Date) rs.get("CREATION_DATE");
      info.rights = (String) rs.get("RIGHTS");
      info.source = (String) rs.get("SOURCE");
      infoOnSources.add(info);
    }
    return infoOnSources;
  }
  

  /**
   * @param id
   * @return
   */
  @GET
  @Path("drug/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<DrugEvidence> getDrugEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getDrugEvidence.sql");

    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "OHDSI_schema"},
            new String[]{String.valueOf(id), tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    final List<DrugEvidence> drugEvidences = new ArrayList<DrugEvidence>();

    //try {
    List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
        //} catch (EmptyResultDataAccessException e) {
    //    log.debug(String.format("Request for conceptId=%s resulted in 0 results", id));
    //    throw new WebApplicationException(Response.Status.RESET_CONTENT); // http 205
    //}

    for (Map rs : rows) {
      String evi_type = (String) rs.get("EV_TYPE");
      boolean modality = (boolean) rs.get("EV_MODALITY");
      String linkout = (String) rs.get("EV_LINKOUT");
      String hoi = String.valueOf((Integer) rs.get("EV_HOI"));
      String statType = (String) rs.get("EV_STAT_TYPE");
      BigDecimal statVal = (BigDecimal) rs.get("EV_STAT_VAL");

      DrugEvidence evidence = new DrugEvidence();
      evidence.evidence = evi_type;
      evidence.modality = modality;
      evidence.linkout = linkout;
      evidence.hoi = hoi;
      evidence.statisticType = statType;
      if (statType.equals("COUNT")) {
        evidence.count = statVal.intValue();
      } else {
        evidence.value = statVal;
      }

      drugEvidences.add(evidence);
    }
    return drugEvidences;
  }

  /**
   * @param id
   * @return
   */
  @GET
  @Path("hoi/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<HoiEvidence> getHoiEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getHoiEvidence.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "OHDSI_schema"},
            new String[]{String.valueOf(id), tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect()); 

    final List<HoiEvidence> hoiEvidences = new ArrayList<HoiEvidence>();

    //try {
    List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
        //} catch (EmptyResultDataAccessException e) {
    //    log.debug(String.format("Request for conceptId=%s resulted in 0 results", id));
    //    throw new WebApplicationException(Response.Status.RESET_CONTENT); // http 205
    //}

    for (Map rs : rows) {
      String evi_type = (String) rs.get("EV_TYPE");
      boolean modality = (boolean) rs.get("EV_MODALITY");
      String linkout = (String) rs.get("EV_LINKOUT");
      String drug = String.valueOf((Integer) rs.get("EV_DRUG"));
      String statType = (String) rs.get("EV_STAT_TYPE");
      BigDecimal statVal = (BigDecimal) rs.get("EV_STAT_VAL");

      HoiEvidence evidence = new HoiEvidence();
      evidence.evidence = evi_type;
      evidence.modality = modality;
      evidence.linkout = linkout;
      evidence.drug = drug;
      evidence.statisticType = statType;
      if (statType.equals("COUNT")) {
        evidence.count = statVal.intValue();
      } else {
        evidence.value = statVal;
      }

      hoiEvidences.add(evidence);
    }
    return hoiEvidences;
  }

  /**
   * @param id
   * @return
   */
  @GET
  @Path("drughoi/{key}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<DrugHoiEvidence> getDrugHoiEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("key") final String key) {
    String[] par = key.split("-");
    String drug_id = par[0];
    String hoi_id = par[1];
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getDrugHoiEvidence.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"drug_id", "hoi_id", "OHDSI_schema"},
            new String[]{drug_id, hoi_id, tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect()); // TODO: why is 'sql server' string passed here?

    final List<DrugHoiEvidence> evidences = new ArrayList<DrugHoiEvidence>();

    //try {
    List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
        //} catch (EmptyResultDataAccessException e) {
    //    log.debug(String.format("Request for conceptId=%s resulted in 0 results", id));
    //    throw new WebApplicationException(Response.Status.RESET_CONTENT); // http 205
    //}

    for (Map rs : rows) {
      String evi_type = (String) rs.get("EV_TYPE");
      boolean modality = (boolean) rs.get("EV_MODALITY");
      String linkout = (String) rs.get("EV_LINKOUT");
      String statType = (String) rs.get("EV_STAT_TYPE");
      BigDecimal statVal = (BigDecimal) rs.get("EV_STAT_VAL");

      DrugHoiEvidence evidence = new DrugHoiEvidence();
      evidence.evidence = evi_type;
      evidence.modality = modality;
      evidence.linkout = linkout;
      evidence.statisticType = statType;
      if (statType.equals("COUNT")) {
        evidence.count = statVal.intValue();
      } else {
        evidence.value = statVal;
      }

      evidences.add(evidence);
    }
    return evidences;
  }

  /**
   * @param id
   * @param filter
   * @return
   */
  @GET
  @Path("drugrollup/{filter}/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<DrugRollUpEvidence> getDrugRollupIngredientEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id, @PathParam("filter") final String filter) {
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String evidenceTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    
    String sql_statement = "";
    if (filter.equals("ingredient")) {
      sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getDrugEvidenceRollupByIngredient.sql");
    } else if (filter.equals("clinicaldrug")) {
      sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getDrugEvidenceRollupByClinicalDrug.sql");
    } else if (filter.equals("brandeddrug")) {
      sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getDrugEvidenceRollupByBrandedDrug.sql");
    } else {
      log.debug(String.format("The call did not specify an appropriate roll up filter (e.g., ingredient, clinicaldrug, brandeddrug)", id));
      return null;
    }

    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "OHDSI_schema", "CDM_schema"},
            new String[]{String.valueOf(id), evidenceTableQualifier, vocabularyTableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect()); // TODO: why is 'sql server' string passed here?

    final List<DrugRollUpEvidence> drugEvidences = new ArrayList<DrugRollUpEvidence>();

    //try {
    List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
        //} catch (EmptyResultDataAccessException e) {
    //    log.debug(String.format("Request for conceptId=%s resulted in 0 results", id));
    //    throw new WebApplicationException(Response.Status.RESET_CONTENT); // http 205
    //}

    for (Map rs : rows) {
      DrugRollUpEvidence evidence = new DrugRollUpEvidence();
      evidence.reportName = (String) rs.get("REPORT_NAME");
      evidence.ingredientId = (Integer) rs.get("INGREDIENT_ID");
      evidence.ingredientName = (String) rs.get("INGREDIENT");
      evidence.clinicalDrugId = (Integer) rs.get("CLINICAL_DRUG_ID");
      evidence.clinicalDrugName = (String) rs.get("CLINICAL_DRUG");
      evidence.hoiId = (Integer) rs.get("HOI_ID");
      evidence.hoiName = (String) rs.get("HOI");
      evidence.pubmedMeshCTcount = (Integer) rs.get("CT_COUNT");
      evidence.pubmedMeshCaseReportcount = (Integer) rs.get("CASE_COUNT");
      evidence.pubmedMeshOthercount = (Integer) rs.get("OTHER_COUNT");
      evidence.splicerCount = (Integer) rs.get("SPLICER_COUNT");
      evidence.euSPCcount = (Integer) rs.get("EU_SPC_COUNT");
      evidence.semmedCTcount = (Integer) rs.get("SEMMEDDB_CT_COUNT");
      evidence.semmedCaseReportcount = (Integer) rs.get("SEMMEDDB_CASE_COUNT");
      evidence.semmedNegCTcount = (Integer) rs.get("SEMMEDDB_NEG_CT_COUNT");
      evidence.semmedNegCaseReportcount = (Integer) rs.get("SEMMEDDB_NEG_CASE_COUNT");
      //evidence.eb05 = (BigDecimal) rs.get("EB05");
      //evidence.ebgm = (BigDecimal) rs.get("EBGM");
      evidence.prr = (BigDecimal) rs.get("PRR");
      evidence.aersReportCount = (Integer) rs.get("AERS_REPORT_COUNT");

      drugEvidences.add(evidence);
    }
    return drugEvidences;
  }
  
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Evidence> getEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getEvidence.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "tableQualifier"},
            new String[]{String.valueOf(id), tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect()); 

    final List<Evidence> evidences = new ArrayList<Evidence>();

    List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
    
    for (Map rs : rows) {
       Evidence e = new Evidence();
      e.conditionId = (Integer) rs.get("CONDITION_ID");
      e.drugId = (Integer) rs.get("DRUG_ID");
      e.drugName = (String) rs.get("DRUG_NAME");
      e.conditionName = (String) rs.get("CONDITION_NAME");
      e.evidenceType = (String) rs.get("EVIDENCE_TYPE");
      e.modality = (boolean) rs.get("MODALITY");
      e.statisticType = (String) rs.get("STATISTIC_TYPE");
      e.linkout = (String) rs.get("EVIDENCE_LINKOUT");
      e.value = (BigDecimal) rs.get("STATISTIC_VALUE");

      evidences.add(e);
    }
    return evidences;
  }  
  
  
  //test
  @GET
  @Path("evidencesummary")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<EvidenceUniverse> getEvidenceSummary(@PathParam("sourceKey") String sourceKey) {
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getEvidenceSummary.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"tableQualifier"},
            new String[]{tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect()); 

    final List<EvidenceUniverse> evidences = new ArrayList<EvidenceUniverse>();

    List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
    
    for (Map rs : rows) {
       EvidenceUniverse e = new EvidenceUniverse();
      e.condition_concept_id = (Integer) rs.get("condition_concept_id");
      e.condition_concept_name = (String) rs.get("condition_concept_name");
      e.ingredient_concept_id = (Integer) rs.get("ingredient_concept_id");
      e.ingredient_concept_name = (String) rs.get("ingredient_concept_name");
      e.evidence_type = (String) rs.get("evidence_type");
      e.modality = (boolean) rs.get("modality");
      e.statistic_value = (BigDecimal) rs.get("statistic_value");
      e.evidence_linkouts = (String) rs.get("evidence_linkouts");

      evidences.add(e);
    }
    return evidences;
  }
  
  
  
  @GET
  @Path("evidencesummary/{drugID}/{conditionID}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<EvidenceSummary> getEvidenceSummaryBySource(@PathParam("sourceKey") String sourceKey,
  																@PathParam("drugID") final long drugID,
  																@PathParam("conditionID") final long conditionID) {
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getEvidenceSummaryBySource.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"drugID","conditionID","tableQualifier"},
            new String[]{String.valueOf(drugID), String.valueOf(conditionID), tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect()); 

    final List<EvidenceSummary> evidences = new ArrayList<EvidenceSummary>();

    List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
    
    for (Map rs : rows) {
    	
      EvidenceSummary e = new EvidenceSummary();
      e.evidence_type = (String) rs.get("evidence_type");
      String tempcount = String.valueOf(rs.get("ct"));
      e.totalNumber = Integer.parseInt(tempcount);

      evidences.add(e);
    }
    return evidences;
  }
  
  @GET
  @Path("evidencedetails/{drugID}/{conditionID}/{evidenceType}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<EvidenceUniverse> getEvidenceDetails(@PathParam("sourceKey") String sourceKey,
  																@PathParam("drugID") final long drugID,
  																@PathParam("conditionID") final long conditionID,
  																@PathParam("evidenceType") final String evidenceType) {
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getEvidenceDetails.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"drugID","conditionID","evidenceType","tableQualifier"},
            new String[]{String.valueOf(drugID), String.valueOf(conditionID), evidenceType, tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect()); 

    final List<EvidenceUniverse> evidences = new ArrayList<EvidenceUniverse>();

    List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
    
    for (Map rs : rows) {
       EvidenceUniverse e = new EvidenceUniverse();
      //e.condition_concept_id = (Integer) rs.get("condition_concept_id");
      //e.condition_concept_name = (String) rs.get("condition_concept_name");
      //e.ingredient_concept_id = (Integer) rs.get("ingredient_concept_id");
      
      //e.ingredient_concept_name = (String) rs.get("ingredient_concept_name");
      e.evidence_linkouts = String.valueOf(rs.get("evidence_linkouts"));
      
      //e.condition_concept_name = (String) rs.get("totalNum");
      //e.modality = (boolean) rs.get("modality");
      //e.statistic_value = (BigDecimal) rs.get("statistic_value");
      //e.evidence_linkouts = (String) rs.get("evidence_linkouts");

      evidences.add(e);
    }
    return evidences;
  }

}
