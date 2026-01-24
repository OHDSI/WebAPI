/*
 * Copyright 2024 Observational Health Data Sciences and Informatics [OHDSI.org].
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

package org.ohdsi.webapi.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO representing an authentication provider configuration for Atlas frontend.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthProviderInfo {

    private String name;
    private String url;
    private boolean ajax;
    private String icon;
    private boolean isUseCredentialsForm;
    private String logoutUrl;

    public AuthProviderInfo() {
    }

    public AuthProviderInfo(String name, String url, boolean ajax, String icon, boolean isUseCredentialsForm) {
        this.name = name;
        this.url = url;
        this.ajax = ajax;
        this.icon = icon;
        this.isUseCredentialsForm = isUseCredentialsForm;
    }

    public AuthProviderInfo(String name, String url, boolean ajax, String icon, boolean isUseCredentialsForm, String logoutUrl) {
        this(name, url, ajax, icon, isUseCredentialsForm);
        this.logoutUrl = logoutUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAjax() {
        return ajax;
    }

    public void setAjax(boolean ajax) {
        this.ajax = ajax;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isUseCredentialsForm() {
        return isUseCredentialsForm;
    }

    public void setUseCredentialsForm(boolean isUseCredentialsForm) {
        this.isUseCredentialsForm = isUseCredentialsForm;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }
}
