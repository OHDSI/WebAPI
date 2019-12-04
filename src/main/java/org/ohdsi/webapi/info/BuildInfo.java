package org.ohdsi.webapi.info;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class BuildInfo {

    private final String artifactVersion;
    private final String build;
    private final String timestamp;

    public BuildInfo(BuildProperties buildProperties, @Value("${build.number}") final String buildNumber) {

        this.artifactVersion = String.format("%s %s", buildProperties.getArtifact(), buildProperties.getVersion());
        this.build = buildNumber;
        this.timestamp = buildProperties.getTime().toString();
    }

    public String getArtifactVersion() {

        return artifactVersion;
    }

    public String getBuild() {

        return build;
    }

    public String getTimestamp() {

        return timestamp;
    }
}
