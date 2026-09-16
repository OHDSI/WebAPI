# OHDSI WebAPI

OHDSI WebAPI contains all OHDSI RESTful services that can be called from OHDSI applications.

## Features

- Provides a centralized API for working with 1 or more databases converted to the [Common Data Model](https://github.com/OHDSI/CommonDataModel) (CDM) v5.
- Searching the OMOP standardized vocabularies for medical concepts and constructing concept sets.
- Defining cohort definitions for use in identifying patient populations.
- Characterizing cohorts
- Computing incidence rates
- Retrieve patient profiles
- Design population level estimation and patient level prediction studies

## Technology

OHDSI WebAPI is a Java 8 web application that utilizes a PostgreSQL database for storage.

## API Documentation

The API Documentation is found at [http://webapidoc.ohdsi.org/](http://webapidoc.ohdsi.org/)

## System Requirements & Installation

Documentation can be found a the [Web API Installation Guide](https://github.com/OHDSI/WebAPI/wiki) which covers the system requirements and installation instructions.

## WebAPI Configuration in version 3.0

Application configuration has moved from a maven build-based pipeline (in version 2.x) to external configuration in WebAPI 3.0 (and using a new YAML format) as described in this [Atlas Sandbox project](https://github.com/OHDSI/AtlasWebAPISandbox/tree/main/ExternalConfig).

### VS.Code Launch settings Example

In VS Code, to launch the app using an external config, you can define a new launch settings in your local .vscode/launch.json file:

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

This will pass the necessary VM arg to load additional Spring configuration from the specified file.  For example, for a local Postgres install with Windows Authentication enabled:

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
### Deploying WAR to Tomcat

You can provide the enviornment variable `spring.config.additional-location` using a context.xml that is uploaded along with the WAR:

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

Sample idp metadata and sp metadata config files for okta:
- `saml/dev/idp-metadata-okta.xml`
- `saml/dev/sp-metadata-okta.xml`

## Managing auth providers (Updated for v3.0)

The following parameters are used to enable/disable certain provider:

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

Acceptable values are `true` and `false`

Default paramaters for each of these authentication providers are provided as an example in the embedded application.yaml file.  All providers are disabled by default.

## Geospatial support

Instructions can be found at [webapi-component-geospatial](https://github.com/OHDSI/webapi-component-geospatial)

## Testing

It was chosen to use embedded PG instead of H2 for unit tests since H2 doesn't support window functions, `md5` function, HEX to BIT conversion, `setval`, `set datestyle`, CTAS + CTE.

## Support

- Developer questions/comments/feedback: [OHDSI forum](http://forums.ohdsi.org/c/developers)
- We use the [GitHub issue tracker](https://github.com/OHDSI/WebAPI/issues) for all bugs/issues/enhancements.

## Contribution

### Versioning

- WebAPI follows [Semantic versioning](https://semver.org/);
- Only Non-SNAPSHOT dependencies should be presented in POM.xml on release branches/tags.

### Development Quick Start Guide

To start the application locally, the following quick steps (all commands are executed from repository root directory)

1. Ensure that you have the following tools installed: Java 1.8, maven (check via `mvn -v`), docker-ce (check via `docker -v`), psql command line client
(check via psql --version) or other tool that allows to connect to postgres DB.
2. Run `mvn clean install` and make sure it completes successfully, resolve dependency issues if any.
3. Create a new database in docker: `docker create --name postgres-webapi -p 8432:5432 -e POSTGRES_PASSWORD=ohdsi postgres:15.0-alpine`.
4. Start DB container: `docker start postgres-webapi`.
	 Verify that you can connect via psql console (`PGPASSWORD='ohdsi' psql -d postgresql://localhost:8432/?user=postgres`).
5. If your default java version is too high (e.g. 17), set JAVA_HOME to point to 1.8 installaction, for example `export JAVA_HOME=/usr/lib/jvm/zulu8-ca-amd64`
6. Start WebAPI `mvn clean install spring-boot:run -Dmaven.test.skip=true -P webapi-postgresql -s src/dev/settings.xml -f pom.xml`
7. Log in with the username of your liking
8. Grant this newly created user admin privileges by running the following sql `INSERT INTO sec_user_role (user_id, role_id, origin) VALUES (1000, 2, 'SYSTEM');`
   and log in again.

At this point you have the application running and admin account operational. To actually use it, additional steps are required to set up privileges
and at least one CDM database. They are covered in the respective documentation sections.

## License
OHDSI WebAPI is licensed under Apache License 2.0