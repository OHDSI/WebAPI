/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package org.ohdsi.webapi.security;

import com.google.common.net.HttpHeaders;
import org.apache.commons.io.IOUtils;
import org.pac4j.core.context.HttpConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * REST Services related to working with Single Sign-On and SAML-based
 * Services
 * 
 * @summary Single Sign On
 */
@Controller
@Path("/saml/")
public class SSOController {
    @Value("${security.saml.metadataLocation}")
    private String metadataLocation;
    @Value("${security.saml.sloUrl}")
    private String sloUri;
    @Value("${security.origin}")
    private String origin;

    /**
     * Get the SAML metadata
     * 
     * @summary Get metadata
     * @param response The response context
     * @throws IOException 
     */
    @GET
    @Path("/saml-metadata")
    public void samlMetadata(@Context HttpServletResponse response) throws IOException {

        ClassPathResource resource = new ClassPathResource(metadataLocation);
        final InputStream is = resource.getInputStream();
        response.setContentType(MediaType.APPLICATION_XML);
        response.setHeader(HttpHeaders.CONTENT_TYPE, "application/xml");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader(HttpHeaders.EXPIRES, "0");
        IOUtils.copy(is, response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * Log out of the service
     * 
     * @summary Log out
     * @return Response
     * @throws URISyntaxException 
     */
    @GET
    @Path("/slo")
    public Response logout() throws URISyntaxException {
        return Response.status(HttpConstants.TEMPORARY_REDIRECT)
                .header(HttpConstants.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, this.origin)
                .location(new URI(sloUri))
                .build();
    }
}
