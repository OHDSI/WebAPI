package org.ohdsi.webapi.generationcache;

import org.junit.Test;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.StatementCancel;
import org.ohdsi.webapi.util.StatementCancelException;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

import static org.mockito.Mockito.mock;

public class GenerationCacheHelperTest {

    @Test(expected = StatementCancelException.class)
    public void runCancelableCohortGenerationSignalsCancellation() throws SQLException {
        StatementCancel statementCancel = new StatementCancel();
        statementCancel.cancel();

        GenerationCacheHelper helper = new GenerationCacheHelper(
                mock(GenerationCacheService.class),
                mock(TransactionTemplate.class)
        );

        helper.runCancelableCohortGeneration(
                mock(CancelableJdbcTemplate.class),
                statementCancel,
                new String[]{"SELECT 1"}
        );
    }
}