package org.ohdsi.webapi.service;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.sqlrender.SourceStatement;
import org.ohdsi.webapi.sqlrender.TranslatedStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Lee Evans
 */
@Path("/sqlrender/")
public class SqlRenderService {
    
    @Context
    ServletContext context;
    @Autowired
    ApplicationContext applicationContext;

    private final String REDSHIFT = "redshift";

    @Path("translate")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TranslatedStatement translateSQL(SourceStatement sourceStatement) {

        TranslatedStatement translated = new TranslatedStatement();
 
        try {
            
            String parameterKeys[] = getMapKeys(sourceStatement.parameters);
            String parameterValues[] = getMapValues(sourceStatement.parameters, parameterKeys);

            String renderedSQL = SqlRender.renderSql(sourceStatement.sql, parameterKeys, parameterValues );
            
            if ((sourceStatement.targetDialect == null) || ("sql server".equals(sourceStatement.targetDialect))) {
                translated.targetSQL = renderedSQL;
            } else {
                translated.targetSQL = SqlTranslate.translateSql(renderedSQL, "sql server", sourceStatement.targetDialect);
            }

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        
        return translated;
    }
    
    private String[] getMapKeys(HashMap<String,String> parameters) {
        if (parameters == null) {
            return null;
        } else {
            return parameters.keySet().toArray(new String[parameters.keySet().size()]);
        }
    }

    private String[] getMapValues(HashMap<String,String> parameters, String[] parameterKeys) {
        ArrayList<String> parameterValues = new ArrayList<>();
        if (parameters == null) {
            return null;
        } else {
            for (String parameterKey : parameterKeys) {
                parameterValues.add((String) parameters.get(parameterKey));
            }
            return parameterValues.toArray(new String[parameterValues.size()]);
        }
    }

    @GET
    @Path("/generate-result/{target}/{schema}")
    @Produces("text/plain")
    public String generateResultSQL(@PathParam("target") String target, @PathParam("schema") String schema) {

        String sql = translateSqlFile("/db/cohort_results/cohort_feasiblity.sql", target, schema);
        sql += "\n" + translateSqlFile("/db/cohort_results/cohort_features_results.sql", target, schema);
        sql += "\n" + translateSqlFile("/db/cohort_results/ir_analysis.sql", target, schema);
        sql += "\n" + translateSqlFile("/db/cohort_results/createHeraclesTables.sql", target, schema);
        if (!REDSHIFT.equalsIgnoreCase(target)) {
            sql += "\n" + translateSqlFile("/db/cohort_results/heracles_indexes.sql", target, schema);
        }
        return sql.replaceAll(";", ";\n");
    }

    private String translateSqlFile(String resource, String target, String schema) {

        SourceStatement statement = new SourceStatement();
        statement.targetDialect = target;
        statement.sql = ResourceHelper.GetResourceAsString(resource);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("results_schema", schema);
        statement.parameters = parameters;
        TranslatedStatement translatedStatement = translateSQL(statement);
        return translatedStatement.targetSQL;
    }
}
