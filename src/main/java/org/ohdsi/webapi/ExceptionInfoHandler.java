/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Alexandr Ryabokon
 *
 */

package org.ohdsi.webapi;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionInfoHandler implements ExceptionMapper<Exception> {

    private static final Map<Class<? extends Exception>, Function<Exception, Response>> functionMap =
            ImmutableMap.<Class<? extends Exception>, Function<Exception, Response>>builder()
                    .put(WebApplicationException.class, e -> ((WebApplicationException) e).getResponse())
                    .build();

    @Override
    public Response toResponse(Exception e) {

        for (Map.Entry<Class<? extends Exception>, Function<Exception, Response>> entry : functionMap.entrySet()) {
            final Class<? extends Exception> key = entry.getKey();
            if (key.isInstance(e)) {
                return entry.getValue().apply(e);
            }
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
}
