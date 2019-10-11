package org.ohdsi.webapi.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.ohdsi.webapi.Constants.SqlSchemaPlaceholders.TEMP_DATABASE_SCHEMA_PLACEHOLDER;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.sqlrender.SourceStatement;
import org.ohdsi.webapi.sqlrender.TranslatedStatement;

@RunWith(MockitoJUnitRunner.class)
public class SqlRenderServiceTest {

    public static final String TEST_SQL = "select cast(field as int) from table; ";

    @Spy
    private SqlRenderService sqlRenderService;

    @Captor
    private ArgumentCaptor<SourceStatement> sourceStatementCaptor;

    @Test
    public void translateSQLFromSourceStatement() {

        SourceStatement statement = createSourceStatement(TEST_SQL, "oracle");
        statement.setOracleTempSchema(null);
        sqlRenderService.translateSQLFromSourceStatement(statement);

        verify(sqlRenderService).translatedStatement(sourceStatementCaptor.capture());
        assertEquals(TEMP_DATABASE_SCHEMA_PLACEHOLDER, sourceStatementCaptor.getValue().getOracleTempSchema());
    }

    @Test
    public void translateSQL_sourceStatementIsNull() {
        assertEquals(new TranslatedStatement(), SqlRenderService.translateSQL(null));
    }

    @Test
    public void translateSQL() {

        SourceStatement statement = createSourceStatement(TEST_SQL, "oracle");
        TranslatedStatement translatedStatement = SqlRenderService.translateSQL(statement);
        assertNotNull(translatedStatement.getTargetSQL());
        assertNotEquals(TEST_SQL, translatedStatement.getTargetSQL());

    }

    @Test
    public void translateSQL_defaultDialect() {

        SourceStatement statement = createSourceStatement(TEST_SQL, Constants.DEFAULT_DIALECT);
        TranslatedStatement translatedStatement = SqlRenderService.translateSQL(statement);
        assertEquals(TEST_SQL, translatedStatement.getTargetSQL());
    }

    private SourceStatement createSourceStatement(String testExpression, String dialect) {

        SourceStatement statement = new SourceStatement();
        statement.setTargetDialect(dialect) ;
        statement.setOracleTempSchema("schema");
        statement.setSql(testExpression);
        statement.getParameters().putAll(Collections.emptyMap());
        return statement;
    }
}