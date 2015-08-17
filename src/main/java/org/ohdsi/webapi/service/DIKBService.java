package org.ohdsi.webapi.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ohdsi.webapi.DIKB.DrugDBModel;
import org.ohdsi.webapi.DIKB.EvidenceDBModel;
import org.ohdsi.webapi.DIKB.InfoDBModel;
import org.ohdsi.webapi.DIKB.SourceDBModel;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.source.JdbcUtil;
import org.springframework.stereotype.Component;

@Path("DIKB/")
@Component
public class DIKBService {
	
	@GET
	@Path("all")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<EvidenceDBModel> getAllEvidence() throws Exception {
		
	    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/localsql/getAllEvidence.sql");
		Connection connection = JdbcUtil.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql_statement);
		List<EvidenceDBModel> evidenceList = new ArrayList<EvidenceDBModel>();
		
		while(resultSet.next())
		{
 	    
			EvidenceDBModel evidence = new EvidenceDBModel();
			evidence.researchStatementLabel = resultSet.getString("researchStatementLabel");
			evidence.assertType = resultSet.getString("assertType");
			evidence.dateAnnotated = resultSet.getString("dateAnnotated");
			evidence.evidenceRole = resultSet.getString("evidenceRole");
			evidence.evidence = resultSet.getString("evidence");
			evidenceList.add(evidence);
 	    
		}
		connection.close();
		return evidenceList;	  
	}
	
	@GET
	@Path("drug")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<DrugDBModel> getAllDrug() throws Exception {
		
	    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/localsql/getAllEvidence.sql");
		Connection connection = JdbcUtil.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql_statement);
		List<DrugDBModel> drugList = new ArrayList<DrugDBModel>();
		List<String> filter = new ArrayList<String>();
		String tempdrug;
		
		while(resultSet.next())
		{
 	    
			tempdrug = resultSet.getString("researchStatementLabel");
			
			
			if(!filter.contains((tempdrug.split("_"))[0]))
			{
				DrugDBModel item = new DrugDBModel();
				item.drugName = (tempdrug.split("_"))[0];
				drugList.add(item);
			}
			filter.add((tempdrug.split("_"))[0]);
		}
		connection.close();
		return drugList;	  
	}
	
	
	
	@GET
	@Path("recent/{num}")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<EvidenceDBModel> getRecentEvidence(@PathParam("num") final String num) throws Exception {
		
	    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/localsql/getRecentEvidence.sql");
	    sql_statement += " LIMIT " + num + ";";
		Connection connection = JdbcUtil.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql_statement);
		List<EvidenceDBModel> evidenceList = new ArrayList<EvidenceDBModel>();
		
		while(resultSet.next())
		{
 	    
			EvidenceDBModel evidence = new EvidenceDBModel();
			evidence.researchStatementLabel = resultSet.getString("researchStatementLabel");
			evidence.assertType = resultSet.getString("assertType");
			evidence.dateAnnotated = resultSet.getString("dateAnnotated");
			evidence.evidenceRole = resultSet.getString("evidenceRole");
			evidence.evidence = resultSet.getString("evidence");
			evidence.source = resultSet.getString("source");
			evidenceList.add(evidence);
 	    
		}
		connection.close();
		return evidenceList;	  
	}
	
	@GET
	@Path("search/{label}")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<EvidenceDBModel> searchEvidence(@PathParam("label") final String label) throws Exception {
		
	    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/localsql/searchEvidenceByLabel.sql");
	    sql_statement += " '%" + label + "%';";
		Connection connection = JdbcUtil.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql_statement);
		List<EvidenceDBModel> evidenceList = new ArrayList<EvidenceDBModel>();
		
		while(resultSet.next())
		{
 	    
			EvidenceDBModel evidence = new EvidenceDBModel();
			evidence.researchStatementLabel = resultSet.getString("researchStatementLabel");
			evidence.assertType = resultSet.getString("assertType");
			evidence.dateAnnotated = resultSet.getString("dateAnnotated");
			evidence.evidenceRole = resultSet.getString("evidenceRole");
			evidence.evidence = resultSet.getString("evidence");
			evidenceList.add(evidence);
 	    
		}
		connection.close();
		return evidenceList;	  
	}
	
	@GET
	@Path("source/{drug}")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<SourceDBModel> getSourceType(@PathParam("drug") final String drug) throws Exception {
		
	    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/localsql/getSourceType.sql");
	    sql_statement = sql_statement.replaceAll("example", drug);
		Connection connection = JdbcUtil.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql_statement);
		List<SourceDBModel> itemList = new ArrayList<SourceDBModel>();
		String tempSourceType;
		
		while(resultSet.next())
		{
 	    
			SourceDBModel item = new SourceDBModel();
			tempSourceType = resultSet.getString("evidenceType");
			if(tempSourceType.length() == 0)
			{
				item.sourceType = "Other";
			}else{
				item.sourceType = tempSourceType.replaceAll("http://dbmi-icode-01.dbmi.pitt.edu/dikb-evidence/DIKB_evidence_ontology_v1.3.owl#", "");
			}
			item.sourceNum = resultSet.getInt("num");
			itemList.add(item);
 	    
		}
		SourceDBModel item1 = new SourceDBModel();
		item1.sourceType = "EV_EX_Trans_Prot_ID";
		item1.sourceNum = 2;
		itemList.add(item1);
		connection.close();
		return itemList;	  
	}
	
	@GET
	@Path("info/{drug}")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<InfoDBModel> getDrugInfo(@PathParam("drug") final String drug) throws Exception {
		
	    String sql_statement = ResourceHelper.GetResourceAsString("/resources/evidence/localsql/searchEvidenceByLabel.sql");
	    sql_statement += " '%" + drug + "%';";
		Connection connection = JdbcUtil.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql_statement);
		List<InfoDBModel> infoList = new ArrayList<InfoDBModel>();
		
		while(resultSet.next())
		{
 	    
			InfoDBModel item = new InfoDBModel();
			String tempLabel = resultSet.getString("researchStatementLabel");
			String predicate = tempLabel.split("_")[1];
			item.predicate = predicate;
			item.precipitant.add(tempLabel.split("_")[2]);
 	    
		}
		connection.close();
		return infoList;	  
	}
	
	
}
