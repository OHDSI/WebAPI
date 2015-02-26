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
import org.ohdsi.webapi.report.MonthlyPrevalence;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

/**
 *
 * @author fdefalco
 */
@Path("/cdmresults/")
@Component
public class CDMResultsService extends AbstractDaoService {

  @Path("{conceptId}/monthlyConditionOccurrencePrevalence")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public MonthlyPrevalence getMonthlyConditionOccurrencePrevalence(@PathParam("conceptId") String conceptId) {

    try {
      String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/getMonthlyConditionOccurrencePrevalence.sql");
      sql_statement = SqlRender.renderSql(sql_statement, new String[]{"OHDSI_schema", "conceptId"}, new String[]{getOhdsiSchema(), conceptId});
      sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());

      return getJdbcTemplate().query(sql_statement, new ResultSetExtractor<MonthlyPrevalence>() {
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
}
