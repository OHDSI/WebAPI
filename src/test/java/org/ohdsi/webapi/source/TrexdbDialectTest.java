package org.ohdsi.webapi.source;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.sql.SqlTranslate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TrexdbDialectTest {

    private static final String SQL =
            "SELECT TOP 10 person_id, DATEADD(day, 30, observation_period_start_date) AS d "
            + "INTO #codesets FROM @cdm.observation_period WHERE ISNULL(period_type_concept_id, 0) > 0;";

    @BeforeClass
    public static void registerDialect() {
        TrexdbDialect.register();
    }

    @Test
    public void translatesLikeItsBaseDialect() {
        String trexdb = SqlTranslate.translateSql(SQL, TrexdbDialect.NAME);
        assertEquals(SqlTranslate.translateSql(SQL, TrexdbDialect.BASE_DIALECT), trexdb);
        assertNotEquals(SQL, trexdb);
    }

    @Test
    public void registerIsIdempotent() {
        TrexdbDialect.register();
        assertEquals(
                SqlTranslate.translateSql(SQL, TrexdbDialect.BASE_DIALECT),
                SqlTranslate.translateSql(SQL, TrexdbDialect.NAME));
    }

    @Test
    public void otherDialectsAreUnchanged() {
        assertNotEquals(
                SqlTranslate.translateSql(SQL, "postgresql"),
                SqlTranslate.translateSql(SQL, TrexdbDialect.NAME));
    }
}
