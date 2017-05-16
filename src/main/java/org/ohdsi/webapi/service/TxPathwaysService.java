package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.person.CohortPerson;
import org.ohdsi.webapi.person.PersonProfile;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.txPathways.Pathway;
import org.ohdsi.webapi.txPathways.TxPathways;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;



@Path("{sourceKey}/txPathways/")
@Component
public class TxPathwaysService extends AbstractDaoService {
  
  @Path("{cohortId}/{conceptSetId}/")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public TxPathways getTxPathways(@PathParam("sourceKey") String sourceKey, @PathParam("cohortId") String cohortId, @PathParam("conceptSetId") String conceptSetId) 
  {
	  final TxPathways txpath = new TxPathways();
	  Source source = getSourceRepository().findBySourceKey(sourceKey);
	  String tableQualifier1="mimic_v5";
	  String tableQualifier2="results_mimic";
	  
	  String sql_statement = ResourceHelper.GetResourceAsString("/resources/txPathways/sql/getTxPathways.sql");
	  
	  sql_statement = SqlRender.renderSql(sql_statement, new String[]{"cohortId", "conceptSetId", "tableQualifier1", "tableQualifier2"}, new String[]{cohortId, conceptSetId, tableQualifier1, tableQualifier2});
	  sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());
	  getSourceJdbcTemplate(source).query(sql_statement, new RowMapper<Void>() {
	      @Override
	      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
	        Pathway item = new Pathway();
	        //item.drug1Rank = resultSet.getInt("drug1_rank");
	        //item.drug1ConceptId = resultSet.getInt("drug1_concept_id");
	        item.drug1ConceptName = resultSet.getString("drug1_concept_name");
	        //item.drug2Rank = resultSet.getInt("drug2_rank");
	        //item.drug2ConceptId = resultSet.getInt("drug2_concept_id");
	        item.drug2ConceptName = resultSet.getString("drug2_concept_name");
	        //item.drug3Rank = resultSet.getInt("drug3_rank");
	        //item.drug3ConceptId = resultSet.getInt("drug3_concept_id");
	        item.drug3ConceptName = resultSet.getString("drug3_concept_name");
	        //item.drug4Rank = resultSet.getInt("drug4_rank");
	        //item.drug4ConceptId = resultSet.getInt("drug4_concept_id");
	        item.drug4ConceptName = resultSet.getString("drug4_concept_name");
	        //item.drug5Rank = resultSet.getInt("drug5_rank");
	        //item.drug5ConceptId = resultSet.getInt("drug5_concept_id");
	        item.drug5ConceptName = resultSet.getString("drug5_concept_name");
	        //item.drug6Rank = resultSet.getInt("drug6_rank");
	        //item.drug6ConceptId = resultSet.getInt("drug6_concept_id");
	        item.drug6ConceptName = resultSet.getString("drug6_concept_name");
	        //item.drug7Rank = resultSet.getInt("drug7_rank");
	        //item.drug7ConceptId = resultSet.getInt("drug7_concept_id");
	        item.drug7ConceptName = resultSet.getString("drug7_concept_name");
	        item.numEdges = resultSet.getInt("num_edges");
	        
	        txpath.pathways.add(item);
	        return null;
	      }
	    });
	    System.out.println("Done");
	    return txpath;
  }
}
