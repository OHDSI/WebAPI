package org.ohdsi.webapi.util;

import java.util.Objects;
import jakarta.ws.rs.NotFoundException;

public class ExceptionUtils {

    public static void throwNotFoundExceptionIfNull(Object entity, String message) throws NotFoundException {

        if (Objects.isNull(entity)) {
            throw new NotFoundException(message);
        }
    }
}
