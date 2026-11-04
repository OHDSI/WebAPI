package org.ohdsi.webapi.source;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.sql.SqlTranslate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TrexsqlDialectTest {

    private static final String SQL =
            "SELECT TOP 10 person_id, DATEADD(day, 30, observation_period_start_date) AS d "
            + "INTO #codesets FROM @cdm.observation_period WHERE ISNULL(period_type_concept_id, 0) > 0;";

    @BeforeClass
    public static void registerDialect() {
        TrexsqlDialect.register();
    }

    @Test
    public void translatesLikeItsBaseDialect() {
        String trexsql = SqlTranslate.translateSql(SQL, TrexsqlDialect.NAME);
        assertEquals(SqlTranslate.translateSql(SQL, TrexsqlDialect.BASE_DIALECT), trexsql);
        assertNotEquals(SQL, trexsql);
    }

    @Test
    public void registerIsIdempotent() {
        TrexsqlDialect.register();
        assertEquals(
                SqlTranslate.translateSql(SQL, TrexsqlDialect.BASE_DIALECT),
                SqlTranslate.translateSql(SQL, TrexsqlDialect.NAME));
    }

    @Test
    public void otherDialectsAreUnchanged() {
        assertNotEquals(
                SqlTranslate.translateSql(SQL, "postgresql"),
                SqlTranslate.translateSql(SQL, TrexsqlDialect.NAME));
    }
}
