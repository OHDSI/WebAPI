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
package org.ohdsi.webapi.shiro.tokens;

import org.apache.shiro.authc.AuthenticationToken;

public class SpnegoToken implements AuthenticationToken {

    byte[] token;

    public SpnegoToken(byte[] token) {

        this.token = token;
    }

    @Override
    public Object getPrincipal() {

        return null;
    }

    @Override
    public Object getCredentials() {

        return token;
    }
}
