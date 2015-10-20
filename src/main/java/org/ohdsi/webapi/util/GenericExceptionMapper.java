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

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.springframework.messaging.support.ErrorMessage;

/**
 *
 * @author fdefalco
 */

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
	
	@Override 
	public Response toResponse(Throwable ex) {
		ErrorMessage errorMessage = new ErrorMessage(ex);
		StringWriter errorStackTrace = new StringWriter();
		ex.printStackTrace(new PrintWriter(errorStackTrace));
 
		return Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity(errorMessage)
				.type(MediaType.APPLICATION_JSON)
				.build();	
	}
}