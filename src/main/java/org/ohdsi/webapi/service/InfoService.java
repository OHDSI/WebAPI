/*
 * Copyright 2017 Observational Health Data Sciences and Informatics [OHDSI.org].
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
package org.ohdsi.webapi.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

/**
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Path("/info")
@Component
public class InfoService {
    private final Info info;

    @Inject
    public InfoService(BuildProperties buildProperties, @Value("${build.number}") final String buildNumber) {
        String artifactVersion = String.format("%s %s", buildProperties.getArtifact(), buildProperties.getVersion());

        this.info = new Info(buildNumber, artifactVersion, buildProperties.getTime().toString());
    }

    public static class Info {
        private final String build;
        private final String timestamp;
        @JsonProperty("version")
        private final String version;

        public Info(String build, String artifactVersion, String timestamp) {
            this.build = build;
            this.version = artifactVersion;
            this.timestamp = timestamp;
        }

        public String getVersion() {
            return version;
        }

        public String getBuild() {
            return build;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Info getInfo() {
        return info;
    }
}
