package org.ohdsi.webapi.util;

import java.util.Objects;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

public class ExceptionUtils {

    public static void throwNotFoundExceptionIfNull(Object entity, String message) {
        if (Objects.isNull(entity)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
    }
}
