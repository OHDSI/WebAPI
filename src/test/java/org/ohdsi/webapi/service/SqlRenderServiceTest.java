package org.ohdsi.webapi.service;

import static org.junit.Assert.*;

import java.util.Collections;
import org.junit.Test;
import org.ohdsi.webapi.sqlrender.SourceStatement;
import org.ohdsi.webapi.sqlrender.TranslatedStatement;

public class SqlRenderServiceTest {

    public static final String TEST_SQL = "select cast(field as int) from table; ";
    private SqlRenderService sqlRenderService = new SqlRenderService();

    @Test
    public void translateSQLFromSourceStatement() {

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

        SourceStatement statement = createSourceStatement(TEST_SQL, SqlRenderService.DEFAULT_DIALECT);
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