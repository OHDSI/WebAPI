# OHDSI WebAPI

OHDSI WebAPI contains all OHDSI RESTful services that can be called from OHDSI applications.

## Features

- Provides a centralized API for working with 1 or more databases converted to the [Common Data Model](https://github.com/OHDSI/CommonDataModel) (CDM) v5.
- Searches the OMOP standardized vocabularies for medical concepts and constructs concept sets.
- Defines cohort definitions for identifying patient populations.
- Characterizes cohorts.
- Computes incidence rates.
- Retrieves patient profiles.

## Technology

OHDSI WebAPI is a Java 21 web application that uses a PostgreSQL database for storage.

## API Documentation

The API Documentation is found at [http://webapidoc.ohdsi.org/](http://webapidoc.ohdsi.org/)

## System Requirements & Installation

Documentation is available in the [WebAPI Installation Guide](https://github.com/OHDSI/WebAPI/wiki), which covers system requirements and installation instructions.

## WebAPI Configuration in version 3.0

Application configuration moved from a Maven build-based pipeline in version 2.x to external YAML configuration in WebAPI 3.0, as described in the [Atlas Sandbox project](https://github.com/OHDSI/AtlasWebAPISandbox/tree/main/ExternalConfig).

### VS Code Launch Configuration Example

In VS Code, to launch the application using an external configuration file, define a new launch configuration in your local `.vscode/launch.json` file:

```
{
  "configurations": [
    {
      "type": "java",
      "name": "WebApi",
      "request": "launch",
      "mainClass": "org.ohdsi.webapi.WebApi",
      "projectName": "WebAPI",
      "vmArgs": "-Dspring.config.additional-location=file:C:/localsource/VSCodeWorkspace/webapi30-application.yaml"
    }
	]
}
```
_Note the format of Windows paths in this example_

This passes the necessary VM argument to load additional Spring configuration from the specified file. For example, for a local PostgreSQL installation with Windows Authentication enabled:

```
datasource:
  dialect: postgresql
  dialect.source: postgresql
  driverClassName: org.postgresql.Driver
  ohdsi:
    schema: webapi
  password: app1
  url: jdbc:postgresql://localhost:5436/OHDSI_30
  username: ohdsi_app_user
security:
  auth:
    windows: 
        enabled: true  
  origin: http://localhost
  provider: AtlasRegularSecurity
```
### Deploying a WAR to Tomcat

You can provide the `spring.config.additional-location` environment property using a `context.xml` file deployed with the WAR:

```
<Context>
    <Environment name="spring.config.additional-location"
                 value="file:/some/path/webapi/config/local-config.yaml"
                 type="java.lang.String"
                 override="false"/>
</Context>
```

## JAR Build (Executable)

WebAPI can also be built as a self-contained executable JAR with embedded Tomcat:

```bash
# Build as JAR
mvn clean package -DskipTests -Dpackaging.type=jar

# Run
java -jar target/WebAPI.jar --spring.profiles.active=webapi-postgresql
```

## Database configuration (single source of truth)

Set your datasource and schema once; the packaged properties reuse the shared schema key.

Minimal local run example (PostgreSQL):

```bash
export WEBAPI_SCHEMA=webapi   # optional; defaults to webapi
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password

java -jar target/WebAPI.jar \
	--spring.profiles.active=webapi-postgresql \
	--datasource.ohdsi.schema=${WEBAPI_SCHEMA:-webapi}
```

Notes:
- Batch uses a table prefix and the security datasource can be overridden if you choose a separate connection, but both are optional when you keep everything on the main datasource/schema.

## SAML Auth support (Updated for 3.0)

The following parameters are used:

- `security.auth.saml.idpMetadataLocation=classpath:saml/dev/idp-metadata.xml` - path to metadata used by identity provider
- `security.auth.saml.metadataLocation=saml/dev/sp-metadata.xml` - service provider metadata path
- `security.auth.saml.keyManager.keyStoreFile=classpath:saml/samlKeystore.jks` - path to keystore
- `security.auth.saml.keyManager.storePassword=nalle123` - keystore password
- `security.auth.saml.keyManager.passwords.arachnenetwork=nalle123` - private key password
- `security.auth.saml.keyManager.defaultKey=apollo` - keystore alias
- `security.auth.saml.sloUrl=https://localhost:8443/cas/logout` - identity provider logout URL
- `security.auth.saml.callbackUrl=http://localhost:8080/WebAPI/user/saml/callback` - URL called from identity provider after login

Sample IdP and SP metadata configuration files for Okta:
- `saml/dev/idp-metadata-okta.xml`
- `saml/dev/sp-metadata-okta.xml`

## Managing Auth Providers (Updated for v3.0)

The following parameters enable or disable authentication providers:

- `security.auth.ad.enabled`
- `security.auth.cas.enabled`
- `security.auth.jdbc.enabled`
- `security.auth.kerberos.enabled`
- `security.auth.ldap.enabled`
- `security.auth.oauth.facebook.enabled`
- `security.auth.oauth.github.enabled`
- `security.auth.oauth.google.enabled`
- `security.auth.openid.enabled`
- `security.auth.windows.enabled`

Acceptable values are `true` and `false`.

Default parameters for each authentication provider are provided as examples in the embedded `application.yaml` file. All providers are disabled by default.

## MCP server for AI agents

WebAPI can expose a [Model Context Protocol](https://modelcontextprotocol.io) server so AI agents can search vocabularies, build concept sets and cohort definitions, validate the cohort SQL and generate cohorts. It is off by default. To turn it on:

```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
```

The server listens at `POST /mcp` (stateless Streamable HTTP; set the path with `spring.ai.mcp.server.streamable-http.mcp-endpoint`). It uses the same authentication as the REST API, so clients send a WebAPI JWT (`Authorization: Bearer ...`) or a personal API key (`X-API-KEY: ...`). Each tool runs with that user's permissions. For example, with Claude Code:

```bash
claude mcp add --transport http webapi https://your-host/WebAPI/mcp --header "X-API-KEY: <key>"
```

| Step | Tools |
|------|-------|
| 1 Concept exploration | `list_sources`, `search_concepts`, `get_concept_details`, `get_concept_hierarchy` |
| 2 Concept sets | `create_concept_set`, `get_concept_set`, `add_concept_to_set`, `remove_concept_from_set`, `count_concept_set_concepts` |
| 3 Cohort criteria | `create_cohort_definition`, `update_cohort_definition`, `get_cohort_definition` |
| 4 SQL validation | `validate_cohort_definition` (Circe checks, concept verification, SqlRender translation; the SQL is not run) |
| 5 Generation | `generate_cohort`, `get_cohort_generation_status` (status, counts, inclusion-rule attrition) |
| Audit | `get_audit_trail` |

Traceability rules the server enforces:

- **Rationale.** Tools that change concept sets or cohort definitions require `clinical_rationale` (at least `mcp.minRationaleLength` characters, default 20) and `evidence_reference`. A call that leaves them out is refused.
- **Verified concepts.** `add_concept_to_set`, `create_cohort_definition` and `update_cohort_definition` look up every concept id in the source vocabulary. If an id does not exist, or the name the caller gave does not match the vocabulary's name, the call is refused and nothing is saved.
- **Tracing.** Send a W3C `traceparent` in the call's `_meta`, plus an optional `reasoning_step`. WebAPI keeps the trace id, records your span as the parent, and returns the tool call's own span in the result's `_meta.traceparent`, along with `_meta.audit_entry_id`. A call without a traceparent starts a new trace, unless `mcp.requireTraceparent: true`.
- **Audit.** Every call is written to `mcp_audit_log` before it runs, then completed with its outcome: `SUCCESS`, `ERROR`, `REJECTED` or `DENIED`. A database trigger makes the table append-only. `get_audit_trail` returns a trace's entries in order, as the decision log for an audit manifest.
- **Provenance.** Calls that create, change, validate or generate a cohort definition or concept set are also recorded against that artifact. `GET /cohortdefinition/{id}/mcp-provenance` and `GET /conceptset/{id}/mcp-provenance` return that history (tool, reasoning step, rationale, evidence, outcome, user, time and trace id; not the argument or result payloads) to anyone who can read the artifact, so Atlas can show why an agent built it that way. These endpoints stay available when the MCP server is turned off.

## Geospatial support

Instructions can be found at [webapi-component-geospatial](https://github.com/OHDSI/webapi-component-geospatial)

## Testing

Embedded PostgreSQL is used instead of H2 for unit tests because H2 does not support window functions, the `md5` function, HEX-to-BIT conversion, `setval`, `set datestyle`, or CTAS with CTEs.

## Support

- Developer questions/comments/feedback: [OHDSI forum](http://forums.ohdsi.org/c/developers)
- We use the [GitHub issue tracker](https://github.com/OHDSI/WebAPI/issues) for all bugs/issues/enhancements.

## Contribution

### Versioning

- WebAPI follows [Semantic versioning](https://semver.org/);
- Only Non-SNAPSHOT dependencies should be presented in POM.xml on release branches/tags.

### Development Quick Start Guide

To start the application locally, follow these steps. Run all commands from the repository root directory.

1. Ensure that you have the following tools installed: JDK 21, Maven (check with `mvn -v`), Docker (check with `docker -v`), and the `psql` command-line client (check with `psql --version`) or another tool that can connect to a PostgreSQL database.
2. Run `mvn clean install` and make sure it completes successfully, resolve dependency issues if any.
3. Create a new database in docker: `docker create --name postgres-webapi -p 8432:5432 -e POSTGRES_PASSWORD=ohdsi postgres:15.0-alpine`.
4. Start DB container: `docker start postgres-webapi`.
  Verify that you can connect through the `psql` console (`PGPASSWORD='ohdsi' psql -d postgresql://localhost:8432/?user=postgres`).
5. Ensure that `JAVA_HOME` points to a JDK 21 installation.
6. Start WebAPI: `mvn clean install spring-boot:run -Dmaven.test.skip=true -P webapi-postgresql -s src/dev/settings.xml -f pom.xml`
7. Log in with the username of your liking
8. Grant this newly created user administrative privileges by running the following SQL: `INSERT INTO sec_user_role (user_id, role_id, origin) VALUES (1000, 2, 'SYSTEM');`
   and log in again.

At this point, the application is running and an administrative account is operational. To use it, additional steps are required to set up privileges
and at least one CDM database. They are covered in the respective documentation sections.

## License
OHDSI WebAPI is licensed under Apache License 2.0