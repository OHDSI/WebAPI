package org.ohdsi.webapi.service;

import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.sqlrender.SourceStatement;
import org.ohdsi.webapi.sqlrender.TranslatedStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

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

    @Path("translate")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TranslatedStatement translateSQLFromSourceStatement(SourceStatement sourceStatement) {

        return translateSQL(sourceStatement);
    }

    static TranslatedStatement translateSQL(SourceStatement sourceStatement) {

        TranslatedStatement translated = new TranslatedStatement();

        try {

            String parameterKeys[] = getMapKeys(sourceStatement.parameters);
            String parameterValues[] = getMapValues(sourceStatement.parameters, parameterKeys);

            String renderedSQL = SqlRender.renderSql(sourceStatement.sql, parameterKeys, parameterValues);

            if ((sourceStatement.targetDialect == null) || ("sql server".equals(sourceStatement.targetDialect))) {
                translated.targetSQL = renderedSQL;
            } else {
                translated.targetSQL = SqlTranslate.translateSql(renderedSQL, sourceStatement.targetDialect);
            }

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return translated;
    }

    private static String[] getMapKeys(HashMap<String, String> parameters) {
        if (parameters == null) {
            return null;
        } else {
            return parameters.keySet().toArray(new String[parameters.keySet().size()]);
        }
    }

    private static String[] getMapValues(HashMap<String, String> parameters, String[] parameterKeys) {
        ArrayList<String> parameterValues = new ArrayList<>();
        if (parameters == null) {
            return null;
        } else {
            for (String parameterKey : parameterKeys) {
                parameterValues.add(parameters.get(parameterKey));
            }
            return parameterValues.toArray(new String[parameterValues.size()]);
        }
    }

}
