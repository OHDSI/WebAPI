package org.ohdsi.webapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.sql.SQLException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;

public class SecurityUtils {

    public static Object whitelist(Object object) {

        return object;
    }

    public static int whitelist(int object) {

        return object;
    }

    public static String whitelist(String object) {

        return object;
    }

    public static String whitelist(Exception exception) {
        if (exception instanceof JobInstanceAlreadyCompleteException) {
            return "Job instance already complete exception";
        } else if (exception instanceof JsonProcessingException) {
            return "Json processing exception";
        } else if (exception instanceof IOException) {
            return "IO exception";
        } else if (exception instanceof TransactionException) {
            return "Transaction exception";
        } else if (exception instanceof DataAccessException) {
            return "Data access exception";
        } else if (exception instanceof SQLException) {
            return "SQL exception";
        }
        return exception.getMessage();
    }

    public static void sleep(int ms) {

        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupted status
        }

    }
}
