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
 * Authors: Alexandr Ryabokon
 *
 */
package org.ohdsi.webapi.shiro.filters.auth;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.ohdsi.webapi.shiro.filters.AuthenticatingPropagationFilter;
import org.springframework.context.ApplicationEventPublisher;

public abstract class AbstractLdapAuthFilter<T extends UsernamePasswordToken> extends AuthenticatingPropagationFilter {
    protected AbstractLdapAuthFilter(ApplicationEventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {

        final String name = request.getParameter("login");
        final String password = request.getParameter("password");
        T token;
        if (name != null && password != null) {
            token = getToken();
            token.setUsername(name);
            token.setPassword(password.toCharArray());
        } else {
            throw new AuthenticationException("Empty credentials");
        }

        return token;
    }

    protected abstract T getToken();

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {

        boolean loggedIn = false;

        if (request.getParameter("login") != null) {
            loggedIn = executeLogin(request, response);
        }

        return loggedIn;
    }
}
