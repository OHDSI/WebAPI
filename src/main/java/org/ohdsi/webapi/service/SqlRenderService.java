package org.ohdsi.webapi.service;

import static org.ohdsi.webapi.Constants.DEFAULT_DIALECT;
import static org.ohdsi.webapi.Constants.SqlSchemaPlaceholders.TEMP_DATABASE_SCHEMA_PLACEHOLDER;

import java.util.Collections;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.sqlrender.SourceStatement;
import org.ohdsi.webapi.sqlrender.TranslatedStatement;
import org.ohdsi.webapi.util.SessionUtils;

/**
 *
 * @author Lee Evans
 */
@Path("/sqlrender/")
public class SqlRenderService {
    /**
     * Translate an OHDSI SQL to a supported target SQL dialect
     * @param sourceStatement JSON with parameters, source SQL, and target dialect
     * @return rendered and translated SQL
     */
    @Path("translate")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public TranslatedStatement translateSQLFromSourceStatement(SourceStatement sourceStatement) {
        if (sourceStatement == null) {
            return new TranslatedStatement();
        }
        sourceStatement.setOracleTempSchema(TEMP_DATABASE_SCHEMA_PLACEHOLDER);
        return translatedStatement(sourceStatement);
    }

    public TranslatedStatement translatedStatement(SourceStatement sourceStatement) {
        return translateSQL(sourceStatement);
    }


    public static TranslatedStatement translateSQL(SourceStatement sourceStatement) {

        TranslatedStatement translated = new TranslatedStatement();
        if (sourceStatement == null) {
            return translated;
        }

        try {
            Map<String, String> parameters = sourceStatement.getParameters() == null ? Collections.emptyMap() : sourceStatement.getParameters();

            String renderedSQL = SqlRender.renderSql(
                    sourceStatement.getSql(),
                    parameters.keySet().toArray(new String[0]),
                    parameters.values().toArray(new String[0]));

            translated.setTargetSQL(translateSql( sourceStatement, renderedSQL));

            return translated;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

    }

    private static String translateSql(SourceStatement sourceStatement, String renderedSQL) {
        if (StringUtils.isEmpty(sourceStatement.getTargetDialect()) || DEFAULT_DIALECT.equals(sourceStatement.getTargetDialect())) {
            return renderedSQL;
        }
        return SqlTranslate.translateSql(renderedSQL, sourceStatement.getTargetDialect(), SessionUtils.sessionId(), sourceStatement.getOracleTempSchema());
    }

}
