/*
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.math.NumberUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.therapy.TherapyPathReport;
import org.ohdsi.webapi.therapy.TherapyPathVector;
import org.ohdsi.webapi.therapy.TherapySummary;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 * @author fdefalco
 *
 * I envision this eventually being subsumed within the cohortresults service
 *
 */
@Path("{sourceKey}/txpathresults/")
@Component
public class TherapyPathResultsService extends AbstractDaoService {

  @Path("reports")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<TherapyPathReport> getReports(@PathParam("sourceKey") String sourceKey) {
    try {

      Source source = getSourceRepository().findBySourceKey(sourceKey);
      PreparedStatementRenderer psr = prepareGetReports(source);
      return getSourceJdbcTemplate(source).query(psr.getSql(),psr.getSetter(), new RowMapper<TherapyPathReport>() {
        @Override
        public TherapyPathReport mapRow(final ResultSet rs, final int arg1) throws SQLException {
          final TherapyPathReport report = new TherapyPathReport();
          report.reportId = rs.getInt(1);
          report.reportCaption = rs.getString(2);
          report.year = Integer.toString(rs.getInt(3));

          if (report.year.equals("9999")) {
            report.year = "All Years";
          }

          report.disease = rs.getString(4);
          report.datasource = rs.getString(5);
          return report;
        }
      });
    } catch (Exception exception) {
      throw new RuntimeException("Error getting therapy path reports" + exception.getMessage());
    }
  }

  protected PreparedStatementRenderer prepareGetReports(Source source) {

    String resourcePath="/resources/therapypathresults/sql/getTherapyPathReports.sql";
    String tqName="OHDSI_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    return new PreparedStatementRenderer(source, resourcePath, tqName, tqValue);
  }

  @Path("report/{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<TherapyPathVector> getTherapyPathVectors(@PathParam("id") String id, @PathParam("sourceKey") String sourceKey) {
    try {
      
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      PreparedStatementRenderer psr = prepareGetTherapyVectors(id, source);
      return getSourceJdbcTemplate(source).query(psr.getSql(),psr.getSetter(), new RowMapper<TherapyPathVector>() {
        @Override
        public TherapyPathVector mapRow(final ResultSet rs, final int arg1) throws SQLException {
          final TherapyPathVector vector = new TherapyPathVector();
          vector.key = rs.getString("ResultKey");
          vector.count = rs.getInt("ResultCount");
          return vector;
        }
      });
    } catch (Exception exception) {
      throw new RuntimeException("Error getting therapy path vectors - " + exception.getMessage());
    }
  }

  protected PreparedStatementRenderer prepareGetTherapyVectors(String id,	Source source) {
    String sqlPath="/resources/therapypathresults/sql/getTherapyPathVectors.sql";
    return new PreparedStatementRenderer(source, sqlPath, null, (String) null, "id", Integer.valueOf(id));
  }

  @Path("summary")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<TherapySummary> getSummaries(@PathParam("sourceKey") String sourceKey, String[] identifiers) {
    try {
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      PreparedStatementRenderer psr = prepareGetSummaries(identifiers, source);
      return getSourceJdbcTemplate(source).query(psr.getSql(),psr.getSetter(), new RowMapper<TherapySummary>() {
        @Override
        public TherapySummary mapRow(final ResultSet rs, final int arg1) throws SQLException {
          final TherapySummary summary = new TherapySummary();
          summary.key = rs.getString(1);
          summary.name = rs.getString(2);
          summary.tx1 = rs.getInt(3);
          summary.tx2 = rs.getInt(4);
          summary.tx3 = rs.getInt(5);
          summary.total = rs.getInt(6);
          return summary;
        }
      });
    } catch (Exception exception) {
      throw new RuntimeException("Error getting therapy path summary - " + exception.getMessage());
    }
  }
  protected PreparedStatementRenderer prepareGetSummaries(String[] identifiers, Source source) {

    String sqlPath = "/resources/therapypathresults/sql/getTherapySummaries.sql";
    List<Integer> values =  Arrays.stream(identifiers).map(NumberUtils::toInt).collect(Collectors.toList());
    return new PreparedStatementRenderer(source, sqlPath, null, (String) null, "identifiersList", values.toArray());
  }
}
