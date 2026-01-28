/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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
 * Authors: Mikhail Mironov, Pavel Grafkin
 *
 */
package org.ohdsi.webapi.shiro.filters.auth;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.ohdsi.webapi.shiro.filters.AuthenticatingPropagationFilter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

public class JdbcAuthFilter extends AuthenticatingPropagationFilter {

    private static final Logger log = LoggerFactory.getLogger(JdbcAuthFilter.class);

    public JdbcAuthFilter(ApplicationEventPublisher eventPublisher){
        super(eventPublisher);
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        final String name = servletRequest.getParameter("login");
        final String password = servletRequest.getParameter("password");
        UsernamePasswordToken token;
        if (name != null && password != null) {
            token = new UsernamePasswordToken(name, password);
        } else {
            throw new AuthenticationException("Empty credentials");
        }
        return token;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        boolean loggedIn = false;

        if (servletRequest.getParameter("login") != null) {
            loggedIn = executeLogin(servletRequest, servletResponse);
        } else {
            // No credentials provided - write error response and commit
            writeUnauthorizedResponse(servletResponse, "Missing credentials");
        }
        return loggedIn;
    }

    private void writeUnauthorizedResponse(ServletResponse response, String message) {
        try {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"" + message + "\"}");
            httpResponse.flushBuffer(); // Commit the response
        } catch (IOException e) {
            log.error("Failed to write unauthorized response", e);
        }
    }

}
