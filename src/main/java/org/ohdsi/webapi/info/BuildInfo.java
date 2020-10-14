package org.ohdsi.webapi.info;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BuildInfo {

    private final String artifactVersion;
    private final String build;
    private final String timestamp;
    private final String branch;
    private final String commitId;
    private final RepositoryInfo atlasRepositoryInfo;
    private final RepositoryInfo webapiRepositoryInfo;

    public BuildInfo(BuildProperties buildProperties, @Value("${build.number}") final String buildNumber) {

        this.artifactVersion = String.format("%s %s", buildProperties.getArtifact(), buildProperties.getVersion());
        this.build = buildNumber;
        this.timestamp = buildProperties.getTime().toString();
        this.branch = buildProperties.get("git.branch");
        this.commitId = buildProperties.get("git.commit.id");
        this.atlasRepositoryInfo = new RepositoryInfo(
                getAsInteger(buildProperties, "atlas.milestone.id"),
                buildProperties.get("atlas.release.tag")
        );
        this.webapiRepositoryInfo = new RepositoryInfo(
                getAsInteger(buildProperties, "webapi.milestone.id"),
                buildProperties.get("webapi.release.tag")
        );
    }

    private Integer getAsInteger(BuildProperties properties, String key) {

        String value = properties.get(key);
        return Optional.ofNullable(value).map(v -> {
            try {
                return StringUtils.isNotBlank(v) ? Integer.valueOf(v) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }).orElse(null);
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

    public String getCommitId() {

        return commitId;
    }

    public String getBranch() {
        return branch;
    }

    public RepositoryInfo getAtlasRepositoryInfo() {
        return atlasRepositoryInfo;
    }

    public RepositoryInfo getWebapiRepositoryInfo() {
        return webapiRepositoryInfo;
    }
}
