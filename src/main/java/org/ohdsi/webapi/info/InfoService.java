/*
 * Copyright 2019 Observational Health Data Sciences and Informatics [OHDSI.org].
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
 *
 * Authors: Christopher Knoll, Pavel Grafkin
 */

package org.ohdsi.webapi.info;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.info.ConfigurationInfo;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Path("/info")
@Controller
public class InfoService {

    private final Info info;

    public InfoService(BuildProperties buildProperties, BuildInfo buildInfo, List<ConfigurationInfo> configurationInfoList) {

        String version = getVersion(buildProperties);

        this.info = new Info(
                version,
                buildInfo,
                configurationInfoList.stream().collect(Collectors.toMap(ConfigurationInfo::getKey, ConfigurationInfo::getProperties))
        );
    }

    /**
     * Get info about the WebAPI instance
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Info getInfo() {

        return info;
    }

    private String getVersion(BuildProperties buildProperties) {

        return StringUtils.split(buildProperties.getVersion(), '-')[0];
    }
}
