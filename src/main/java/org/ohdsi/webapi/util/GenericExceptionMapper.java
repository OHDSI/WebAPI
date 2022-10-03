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

import org.ohdsi.webapi.exception.BadRequestAtlasException;
import org.ohdsi.webapi.exception.ConceptNotExistException;
import org.ohdsi.webapi.exception.ConversionAtlasException;
import org.ohdsi.webapi.exception.UserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.support.ErrorMessage;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;
import org.ohdsi.webapi.vocabulary.ConceptRecommendedNotInstalledException;

/**
 *
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
        } else if (ex instanceof UnauthorizedException || ex instanceof ForbiddenException) {
            responseStatus = Status.FORBIDDEN;
        } else if (ex instanceof NotFoundException) {
            responseStatus = Status.NOT_FOUND;
        } else if (ex instanceof BadRequestException) {
            responseStatus = Status.BAD_REQUEST;
        } else if (ex instanceof UndeclaredThrowableException) {
            Throwable throwable = getThrowable((UndeclaredThrowableException)ex);
            if (Objects.nonNull(throwable)) {
                if (throwable instanceof UnauthorizedException || throwable instanceof ForbiddenException) {
                    responseStatus = Status.FORBIDDEN;
                } else if (throwable instanceof BadRequestAtlasException || throwable instanceof ConceptNotExistException) {
                    responseStatus = Status.BAD_REQUEST;
                    ex = throwable;
                } else if (throwable instanceof ConversionAtlasException) {
                    responseStatus = Status.BAD_REQUEST;
                    // New exception must be created or direct self-reference exception will be thrown
                    ex = new RuntimeException(throwable.getMessage());
                } else {
                    responseStatus = Status.INTERNAL_SERVER_ERROR;
                    ex = new RuntimeException("An exception occurred: " + ex.getClass().getName());
                }
            } else {
                responseStatus = Status.INTERNAL_SERVER_ERROR;
                ex = new RuntimeException("An exception occurred: " + ex.getClass().getName());
            }
        } else if (ex instanceof UserException) {
            responseStatus = Status.INTERNAL_SERVER_ERROR;
            // Create new message to prevent sending error information to client
            ex = new RuntimeException(ex.getMessage());
        } else if (ex instanceof ConceptNotExistException) {
            responseStatus = Status.BAD_REQUEST;
        } else if (ex instanceof ConceptRecommendedNotInstalledException) {
          responseStatus = Status.NOT_IMPLEMENTED;
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

    private Throwable getThrowable(UndeclaredThrowableException ex) {
        if (Objects.nonNull(ex.getUndeclaredThrowable()) && ex.getUndeclaredThrowable() instanceof InvocationTargetException) {
            InvocationTargetException ite = (InvocationTargetException) ex.getUndeclaredThrowable();
            return ite.getTargetException();
        }
        return null;
    }
}