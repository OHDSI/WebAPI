package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
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
import org.ohdsi.webapi.source.SourceDaimon;
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
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

      String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/getMonthlyConditionOccurrencePrevalence.sql");
      sql_statement = SqlRender.renderSql(sql_statement, new String[]{"OHDSI_schema", "conceptId"}, new String[]{tableQualifier, conceptId});
      sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

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

  private final RowMapper<SimpleEntry<Long, Long[]>> rowMapper = new RowMapper<SimpleEntry<Long, Long[]>>() {
    @Override
    public SimpleEntry<Long, Long[]> mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
      long id = resultSet.getLong("concept_id");
      long record_count = resultSet.getLong("record_count");
      long descendant_record_count = resultSet.getLong("descendant_record_count");

      SimpleEntry<Long, Long[]> entry = new SimpleEntry<Long, Long[]>(id, new Long[] { record_count, descendant_record_count });
      return entry;
    }
  };

  @Path("conceptRecordCount")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<SimpleEntry<Long, Long[]>> getConceptRecordCount(@PathParam("sourceKey") String sourceKey, String[] identifiers) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

    for (int i = 0; i < identifiers.length; i++) {
      identifiers[i] = "'" + identifiers[i] + "'";
    }

    String identifierList = StringUtils.join(identifiers, ",");
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/getConceptRecordCount.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"tableQualifier", "conceptIdentifiers"}, new String[]{tableQualifier, identifierList});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    return getSourceJdbcTemplate(source).query(sql_statement, rowMapper);
  }
}
