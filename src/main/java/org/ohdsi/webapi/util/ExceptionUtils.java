package org.ohdsi.webapi.util;

import javax.ws.rs.NotFoundException;
import java.util.Objects;

public class ExceptionUtils {

    public static void throwNotFoundExceptionIfNull(Object entity, String message) throws NotFoundException {

        if (Objects.isNull(entity)) {
            throw new NotFoundException(message);
        }
    }
}
