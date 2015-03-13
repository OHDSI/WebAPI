package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;

import org.ohdsi.webapi.helper.ResourceHelper;

import org.ohdsi.webapi.evidence.DrugEvidence;
import org.ohdsi.webapi.evidence.HoiEvidence;
import org.ohdsi.webapi.evidence.DrugHoiEvidence;
import org.ohdsi.webapi.evidence.EvidenceInfo;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author rkboyce based on the vocabulary service written by fdefalco and an initial api written by m_rasteger
 */
@Path("/evidence/")
@Component
public class EvidenceService extends AbstractDaoService {
    
    /**
     * @param id
     * @return
     */
    @GET
    @Path("drug/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<DrugEvidence> getDrugEvidence(@PathParam("id") final Long id) {
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getDrugEvidence.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "id", "CDM_schema" }, 
					    new String[] { String.valueOf(id), getCdmSchema()});
	sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect()); // TODO: why is 'sql server' string passed here?

        final List<DrugEvidence> drugEvidences = new ArrayList<DrugEvidence>();

	//try {
	List<Map<String, Object>> rows =  getJdbcTemplate().queryForList(sql_statement);
        //} catch (EmptyResultDataAccessException e) {
        //    log.debug(String.format("Request for conceptId=%s resulted in 0 results", id));
        //    throw new WebApplicationException(Response.Status.RESET_CONTENT); // http 205
        //}

	// count the number of evidence items with the same evidence type, modality, linkout, and hoi
	int i = 0;
	HashMap<String, Integer> hmHoiCount = new HashMap<String, Integer>();
	for (Map rs : rows) {
	    i++;
	    String evi_type = (String)rs.get("EV_TYPE");
	    String modality = String.valueOf((Boolean)rs.get("EV_MODALITY"));
	    String linkout = (String)rs.get("EV_LINKOUT");
	    String hoi = String.valueOf((Integer)rs.get("EV_HOI"));
	    String hoiConcat = evi_type + "_-_" + modality + "_-_" +  linkout + "_-_" +  hoi;
	    if (hmHoiCount.containsKey(hoiConcat)) {
		hmHoiCount.put(hoiConcat, hmHoiCount.get(hoiConcat) + 1);
	    } else {
		hmHoiCount.put(hoiConcat, 1);
	    }
	}
	
	// construct the return collection
	Iterator<Map.Entry<String, Integer>> it = hmHoiCount.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String, Integer> ent = it.next();
	    String[] hoiConcat = ent.getKey().split("_-_");     
	    DrugEvidence drugEvidence = new DrugEvidence();
	    drugEvidence.evidence =  hoiConcat[0];
	    drugEvidence.modality = hoiConcat[1];
	    drugEvidence.linkout =  hoiConcat[2];
	    drugEvidence.hoi = hoiConcat[3];
	    drugEvidence.count =  ent.getValue();
	    drugEvidences.add(drugEvidence);
	}
	// TODO: return a total number of evidence items somehow
	return drugEvidences;
    }


    /**
     * @param id
     * @return
     */
    @GET
    @Path("hoi/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<HoiEvidence> getHoiEvidence(@PathParam("id") final Long id) {
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getHoiEvidence.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "id", "CDM_schema" }, 
					    new String[] { String.valueOf(id), getCdmSchema()});
	sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect()); // TODO: why is 'sql server' string passed here?

        final List<HoiEvidence> hoiEvidences = new ArrayList<HoiEvidence>();

	//try {
	List<Map<String, Object>> rows =  getJdbcTemplate().queryForList(sql_statement);
        //} catch (EmptyResultDataAccessException e) {
        //    log.debug(String.format("Request for conceptId=%s resulted in 0 results", id));
        //    throw new WebApplicationException(Response.Status.RESET_CONTENT); // http 205
        //}

	// count the number of evidence items with the same evidence type, modality, linkout, and hoi
	int i = 0;
	HashMap<String, Integer> hmHoiCount = new HashMap<String, Integer>();
	for (Map rs : rows) {
	    i++;
	    String evi_type = (String)rs.get("EV_TYPE");
	    String modality = String.valueOf((Boolean)rs.get("EV_MODALITY"));
	    String linkout = (String)rs.get("EV_LINKOUT");
	    String drug = String.valueOf((Integer)rs.get("EV_DRUG"));
	    String hoiConcat = evi_type + "_-_" + modality + "_-_" +  linkout + "_-_" +  drug;
	    if (hmHoiCount.containsKey(hoiConcat)) {
		hmHoiCount.put(hoiConcat, hmHoiCount.get(hoiConcat) + 1);
	    } else {
		hmHoiCount.put(hoiConcat, 1);
	    }
	}
	
	// construct the return collection
	Iterator<Map.Entry<String, Integer>> it = hmHoiCount.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String, Integer> ent = it.next();
	    String[] hoiConcat = ent.getKey().split("_-_");     
	    HoiEvidence hoiEvidence = new HoiEvidence();
	    hoiEvidence.evidence =  hoiConcat[0];
	    hoiEvidence.modality = hoiConcat[1];
	    hoiEvidence.linkout =  hoiConcat[2];
	    hoiEvidence.drug = hoiConcat[3];
	    hoiEvidence.count =  ent.getValue();
	    hoiEvidences.add(hoiEvidence);
	}
	// TODO: return a total number of evidence items somehow
	return hoiEvidences;
    }

    /**
     * @param id
     * @return
     */
    @GET
    @Path("drughoi/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<DrugHoiEvidence> getDrugHoiEvidence(@PathParam("key") final String key) {
	String[] par = key.split("-");
	String drug_id = par[0];
	String hoi_id = par[1];
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getDrugHoiEvidence.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "drug_id", "hoi_id",  "CDM_schema" }, 
					    new String[] {drug_id, hoi_id , getCdmSchema()});
	sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect()); // TODO: why is 'sql server' string passed here?

        final List<DrugHoiEvidence> evidences = new ArrayList<DrugHoiEvidence>();

	//try {
	List<Map<String, Object>> rows =  getJdbcTemplate().queryForList(sql_statement);
        //} catch (EmptyResultDataAccessException e) {
        //    log.debug(String.format("Request for conceptId=%s resulted in 0 results", id));
        //    throw new WebApplicationException(Response.Status.RESET_CONTENT); // http 205
        //}

	// count the number of evidence items with the same evidence type, modality, linkout, and hoi
	int i = 0;
	HashMap<String, Integer> hmHoiCount = new HashMap<String, Integer>();
	for (Map rs : rows) {
	    i++;
	    String evi_type = (String)rs.get("EV_TYPE");
	    String modality = String.valueOf((Boolean)rs.get("EV_MODALITY"));
	    String linkout = (String)rs.get("EV_LINKOUT");
	    String hoiConcat = evi_type + "_-_" + modality + "_-_" +  linkout;
	    if (hmHoiCount.containsKey(hoiConcat)) {
		hmHoiCount.put(hoiConcat, hmHoiCount.get(hoiConcat) + 1);
	    } else {
		hmHoiCount.put(hoiConcat, 1);
	    }
	}
	
	// construct the return collection
	Iterator<Map.Entry<String, Integer>> it = hmHoiCount.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String, Integer> ent = it.next();
	    String[] hoiConcat = ent.getKey().split("_-_");     
	    DrugHoiEvidence evidence = new DrugHoiEvidence();
	    evidence.evidence =  hoiConcat[0];
	    evidence.modality = hoiConcat[1];
	    evidence.linkout =  hoiConcat[2];
	    evidence.count =  ent.getValue();
	    evidences.add(evidence);
	}
	// TODO: return a total number of evidence items somehow
	return evidences;
    }


    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<EvidenceInfo> getInfo() {      

        String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/sql/getInfo.sql");        
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "CDM_schema" }, new String[] { getCdmSchema() });
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());  // TODO: why is 'sql server' string passed here?

	final List<EvidenceInfo> infoOnSources = new ArrayList<EvidenceInfo>();
	
	//try {
	List<Map<String, Object>> rows =  getJdbcTemplate().queryForList(sql_statement);
        //} catch (EmptyResultDataAccessException e) {
        //    log.debug(String.format("Request for conceptId=%s resulted in 0 results", id));
        //    throw new WebApplicationException(Response.Status.RESET_CONTENT); // http 205
        //}

	for (Map rs : rows) {
	    EvidenceInfo info = new EvidenceInfo();
	    info.title = (String)rs.get("TITLE");
	    info.description = (String)rs.get("DESCRIPTION");
	    info.contributer = (String)rs.get("CONTRIBUTER");
	    info.creator = (String)rs.get("CREATOR");
	    info.creationDate = (Date)rs.get("CREATION_DATE");
	    info.rights = (String)rs.get("RIGHTS");
	    info.source = (String)rs.get("SOURCE");
	    infoOnSources.add(info);
	}
	return infoOnSources;
    }
    

}
