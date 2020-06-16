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

import org.ohdsi.webapi.shiro.tokens.ActiveDirectoryUsernamePasswordToken;
import org.springframework.context.ApplicationEventPublisher;

public class ActiveDirectoryAuthFilter extends AbstractLdapAuthFilter<ActiveDirectoryUsernamePasswordToken> {

    public ActiveDirectoryAuthFilter(ApplicationEventPublisher eventPublisher){
        super(eventPublisher);
    }

    @Override
    protected ActiveDirectoryUsernamePasswordToken getToken() {

        return new ActiveDirectoryUsernamePasswordToken();
    }
}
