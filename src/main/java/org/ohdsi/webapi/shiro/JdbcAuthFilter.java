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
package org.ohdsi.webapi.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class JdbcAuthFilter extends AuthenticatingFilter {

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        final String name = servletRequest.getParameter("login");
        final String password = servletRequest.getParameter("password");
        UsernamePasswordToken token;
        if (name!=null && password!=null){
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
            try {
                loggedIn = executeLogin(servletRequest, servletResponse);
            } catch(AuthenticationException ae) {
                loggedIn = false;
            }
        }

        if (!loggedIn) {
            HttpServletResponse httpResponse = WebUtils.toHttp(servletResponse);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return loggedIn;
    }
}
