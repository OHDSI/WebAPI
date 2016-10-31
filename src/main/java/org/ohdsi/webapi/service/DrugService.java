/*
 * Copyright 2015 sigfried gold. (starting from code written by fdefalco)
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

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.person.ObservationPeriod;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.person.PersonRecord;
import org.ohdsi.webapi.person.CohortPerson;
import org.ohdsi.webapi.person.PersonProfile;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONException;
@Path("{sourceKey}/drugexp/")
@Component
public class DrugService extends AbstractDaoService {

  @Autowired 
  private VocabularyService vocabService;
  
  @Autowired
  private ConceptSetService conceptSetService;
  
  @Path("personId/{personId}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getPersonProfile(@PathParam("sourceKey") String sourceKey, @PathParam("personId") String personId) 
          throws JSONException
  {    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    
    final JSONArray ja = new JSONArray();
    JSONObject jo = new JSONObject("{'a':1, 'b':2}");
    ja.put(jo);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/drugs/sql/drugIngredient.sql");
    sql_statement = sql_statement + " where dex.person_id = @personId";
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"personId", "tableQualifier"}, new String[]{personId, tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());
    
    System.out.println(sql_statement);
    
    getSourceJdbcTemplate(source).query(sql_statement, new RowMapper<Void>() {
      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException  {
        int column_count = resultSet.getMetaData().getColumnCount();
        JSONObject obj = new JSONObject();
        try {
            for (int i = 0; i < column_count; i++) {
                obj.put(resultSet.getMetaData().getColumnLabel(i + 1)
                    .toLowerCase(), resultSet.getObject(i + 1));       
            }
            ja.put(obj);
        } catch(JSONException e) {
            System.err.println(e.toString());
        }
        return null;
      }
    });
    return ja.toString();
  }
}
