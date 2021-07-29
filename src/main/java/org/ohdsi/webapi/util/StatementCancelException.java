package org.ohdsi.webapi.util;

public class StatementCancelException extends RuntimeException {
    @Override
    public String getMessage() {
        return "statement cannot be set - already cancelled";
    }
}
