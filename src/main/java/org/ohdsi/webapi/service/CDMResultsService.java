package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.report.MonthlyPrevalence;
import org.ohdsi.webapi.report.PackedConceptNode;
import org.ohdsi.webapi.source.Source;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 * @author fdefalco
 */
@Path("{sourceKey}/cdmresults/")
@Component
public class CDMResultsService extends AbstractDaoService {

  @Path("{conceptId}/monthlyConditionOccurrencePrevalence")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public MonthlyPrevalence getMonthlyConditionOccurrencePrevalence(@PathParam("sourceKey") String sourceKey, @PathParam("conceptId") String conceptId) {
    try {
      String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/getMonthlyConditionOccurrencePrevalence.sql");
      sql_statement = SqlRender.renderSql(sql_statement, new String[]{"OHDSI_schema", "conceptId"}, new String[]{getOhdsiSchema(), conceptId});
      sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());

      Source source = getSourceRepository().findBySourceKey(sourceKey);

      return getSourceJdbcTemplate(source).query(sql_statement, new ResultSetExtractor<MonthlyPrevalence>() {
        @Override
        public MonthlyPrevalence extractData(ResultSet rs) throws SQLException, DataAccessException {
          MonthlyPrevalence result = new MonthlyPrevalence();
          while (rs.next()) {
            result.monthKey.add(rs.getString(1));
            result.prevalence.add(rs.getFloat(2));
          }
          return result;
        }
      });
    } catch (Exception exception) {
      throw new RuntimeException("Error retrieving monthly condition occurrence prevalence statistics." + exception.getMessage());
    }
  }

  private PackedConceptNode root;
  
  private final RowMapper<PackedConceptNode> rowMapper = new RowMapper<PackedConceptNode>() {
    @Override
    public PackedConceptNode mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
      final PackedConceptNode node = new PackedConceptNode();
      node.conceptId = resultSet.getLong("CONCEPT_ID");
      node.size = resultSet.getLong("NUM_RECORDS");
      return node;
    }
  };

  @Path("conceptRecordCount")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PackedConceptNode getConceptRecordCount(@PathParam("sourceKey") String sourceKey, String[] identifiers) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);

   for (int i=0; i<identifiers.length; i++) {
     identifiers[i] = "'" + identifiers[i] + "'";
   }

    String identifierList = StringUtils.join(identifiers, ",");
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/getConceptRecordCount.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"OHDSI_schema", "conceptIdentifiers"}, new String[]{getOhdsiSchema(), identifierList});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    List<PackedConceptNode> nodes;
    nodes = getSourceJdbcTemplate(source).query(sql_statement, rowMapper);
    
    PackedConceptNode root = new PackedConceptNode();
    root.conceptName = "root";
    root.children = nodes;
    return root;
  }

}
