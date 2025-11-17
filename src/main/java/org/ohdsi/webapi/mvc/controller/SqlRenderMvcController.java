package org.ohdsi.webapi.mvc.controller;

import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.service.SqlRenderService;
import org.ohdsi.webapi.sqlrender.SourceStatement;
import org.ohdsi.webapi.sqlrender.TranslatedStatement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.ohdsi.webapi.Constants.SqlSchemaPlaceholders.TEMP_DATABASE_SCHEMA_PLACEHOLDER;

/**
 * Spring MVC version of SqlRenderService
 *
 * Migration Status: Replaces /service/SqlRenderService.java (Jersey)
 * Endpoints: 1 POST endpoint
 * Complexity: Simple - POST with JSON request body
 */
@RestController
@RequestMapping("/sqlrender")
public class SqlRenderMvcController extends AbstractMvcController {

    /**
     * Translate an OHDSI SQL to a supported target SQL dialect
     *
     * Jersey: POST /WebAPI/sqlrender/translate
     * Spring MVC: POST /WebAPI/v2/sqlrender/translate
     *
     * @param sourceStatement JSON with parameters, source SQL, and target dialect
     * @return rendered and translated SQL
     */
    @PostMapping(
        value = "/translate",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TranslatedStatement> translateSQLFromSourceStatement(@RequestBody SourceStatement sourceStatement) {
        if (sourceStatement == null) {
            return ok(new TranslatedStatement());
        }
        sourceStatement.setOracleTempSchema(TEMP_DATABASE_SCHEMA_PLACEHOLDER);
        TranslatedStatement result = SqlRenderService.translateSQL(sourceStatement);
        return ok(result);
    }
}
