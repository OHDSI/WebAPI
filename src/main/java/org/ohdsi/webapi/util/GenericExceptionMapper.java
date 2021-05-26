/*
 * Copyright 2015 fdefalco.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.util;

import org.apache.shiro.authz.UnauthorizedException;
import org.ohdsi.webapi.exception.BadRequestAtlasException;
import org.ohdsi.webapi.exception.ConversionAtlasException;
import org.ohdsi.webapi.exception.UserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.support.ErrorMessage;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author fdefalco
 */

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericExceptionMapper.class);
    private final String DETAIL = "Detail: ";

    @Override
    public Response toResponse(Throwable ex) {
        StringWriter errorStackTrace = new StringWriter();
        ex.printStackTrace(new PrintWriter(errorStackTrace));
        LOGGER.error(errorStackTrace.toString());
        Status responseStatus;
        if (ex instanceof DataIntegrityViolationException) {
            responseStatus = Status.CONFLICT;
            String cause = ex.getCause().getCause().getMessage();
            cause = cause.substring(cause.indexOf(DETAIL) + DETAIL.length());
            ex = new RuntimeException(cause);
        } else if (ex instanceof UnauthorizedException) {
            responseStatus = Status.FORBIDDEN;
        } else if (ex instanceof NotFoundException) {
            responseStatus = Status.NOT_FOUND;
        } else if (ex instanceof ConversionFailedException) {
            responseStatus = Status.BAD_REQUEST;
        } else if (ex instanceof BadRequestAtlasException) {
            responseStatus = Status.BAD_REQUEST;
        } else if (ex instanceof UserException) {
            responseStatus = Status.INTERNAL_SERVER_ERROR;
            // Create new message to prevent sending error information to client
            ex = new RuntimeException(ex.getMessage());
        } else {
            responseStatus = Status.INTERNAL_SERVER_ERROR;
            // Create new message to prevent sending error information to client
            ex = new RuntimeException("An exception occurred: " + ex.getClass().getName());
        }
        // Clean stacktrace, but keep message
        ex.setStackTrace(new StackTraceElement[0]);
        ErrorMessage errorMessage = new ErrorMessage(ex);
        return Response.status(responseStatus)
                .entity(errorMessage)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}