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
 * Authors: Pavel Grafkin
 *
 */
package org.ohdsi.webapi.shiro.filters;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.tokens.SpnegoToken;

public class KerberosAuthFilter extends AuthenticatingFilter {

    private String getAuthHeader(ServletRequest servletRequest) {

        HttpServletRequest request = WebUtils.toHttp(servletRequest);
        return request.getHeader("Authorization");
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {

        String authHeader = getAuthHeader(servletRequest);
        AuthenticationToken authToken = null;

        if (authHeader != null) {
            byte[] token = Base64.decode(authHeader.replaceAll("^Negotiate ", ""));
            authToken = new SpnegoToken(token);
        }

        return authToken;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {

        boolean loggedIn = false;
        String authHeader = getAuthHeader(servletRequest);

        if (authHeader != null) {
            try {
                loggedIn = executeLogin(servletRequest, servletResponse);
            } catch (AuthenticationException ae) {
                loggedIn = false;
            }
        }

        if (!loggedIn) {
            HttpServletResponse response = WebUtils.toHttp(servletResponse);
            response.addHeader("WWW-Authenticate", "Negotiate");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return loggedIn;
    }
}
