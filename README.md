# OHDSI WebAPI

OHDSI WebAPI contains all OHDSI RESTful services that can be called from OHDSI applications.

## Getting Started

**Download and run the latest release:**

```bash
# Download the JAR
wget https://github.com/OHDSI/WebAPI/releases/latest/download/WebAPI.jar

# Run with PostgreSQL
java -jar WebAPI.jar --spring.profiles.active=webapi-postgresql
```

**That's it!** WebAPI is packaged as a self-contained executable JAR with embedded Tomcat server. No external application server required.

For detailed setup instructions, see the [Quick Start Guide](#quick-start-guide) below.

## Features

- Provides a centralized API for working with 1 or more databases converted to the [Common Data Model](https://github.com/OHDSI/CommonDataModel) (CDM) v5.
- Searching the OMOP standardized vocabularies for medical concepts and constructing concept sets.
- Defining cohort definitions for use in identifying patient populations.
- Characterizing cohorts
- Computing incidence rates
- Retrieve patient profiles
- Design population level estimation and patient level prediction studies

## Technology

OHDSI WebAPI is a **Java 21** Spring Boot 3.5.6 application packaged as an **executable JAR** with embedded Tomcat server. It supports multiple databases including PostgreSQL, SQL Server, Oracle, and others.

## API Documentation

The API Documentation is found at [http://webapidoc.ohdsi.org/](http://webapidoc.ohdsi.org/)

## System Requirements & Installation

Documentation can be found a the [Web API Installation Guide](https://github.com/OHDSI/WebAPI/wiki) which covers the system requirements and installation instructions.

## JAR Build (Executable)

WebAPI can also be built as a self-contained executable JAR with embedded Tomcat:

```bash
# Build as JAR
mvn clean package -DskipTests -Dpackaging.type=jar

# Run
java -jar target/WebAPI.jar --spring.profiles.active=webapi-postgresql
```

## SAML Auth support

The following parameters are used:

- `security.saml.idpMetadataLocation=classpath:saml/dev/idp-metadata.xml` - path to metadata used by identity provider
- `security.saml.metadataLocation=saml/dev/sp-metadata.xml` - service provider metadata path
- `security.saml.keyManager.keyStoreFile=classpath:saml/samlKeystore.jks` - path to keystore
- `security.saml.keyManager.storePassword=nalle123` - keystore password
- `security.saml.keyManager.passwords.arachnenetwork=nalle123` - private key password
- `security.saml.keyManager.defaultKey=apollo` - keystore alias
- `security.saml.sloUrl=https://localhost:8443/cas/logout` - identity provider logout URL
- `security.saml.callbackUrl=http://localhost:8080/WebAPI/user/saml/callback` - URL called from identity provider after login

Sample idp metadata and sp metadata config files for okta:
- `saml/dev/idp-metadata-okta.xml`
- `saml/dev/sp-metadata-okta.xml`

## Security Configuration

### Managing Authentication Providers

WebAPI supports multiple authentication providers that can be enabled/disabled via configuration:

```bash
java -jar WebAPI.jar \
  --security.auth.google.enabled=true \
  --security.oauth.google.apiKey=your-client-id \
  --security.oauth.google.apiSecret=your-client-secret
```

Available authentication providers:

| Provider | Property | Description |
|----------|----------|-------------|
| Windows Auth | `security.auth.windows.enabled` | Windows integrated authentication |
| Kerberos | `security.auth.kerberos.enabled` | Kerberos authentication |
| OpenID Connect | `security.auth.openid.enabled` | OpenID Connect (OIDC) |
| OAuth - Google | `security.auth.google.enabled` | Google OAuth 2.0 |
| OAuth - Facebook | `security.auth.facebook.enabled` | Facebook OAuth 2.0 |
| OAuth - GitHub | `security.auth.github.enabled` | GitHub OAuth 2.0 |
| LDAP | `security.auth.ldap.enabled` | LDAP authentication |
| Active Directory | `security.auth.ad.enabled` | Active Directory |
| JDBC | `security.auth.jdbc.enabled` | Database authentication |
| CAS | `security.auth.cas.enabled` | Central Authentication Service |

All properties accept `true` or `false`.

**Example: Enable Google OAuth:**
```properties
security.auth.google.enabled=true
security.oauth.google.apiKey=your-google-client-id
security.oauth.google.apiSecret=your-google-client-secret
security.oauth.callback.api=http://localhost:8080/WebAPI/user/oauth/callback
security.oauth.callback.ui=http://localhost:8080/atlas/#/welcome
```

**Example: Disable all authentication (development only):**
```bash
java -jar WebAPI.jar --security.provider=DisabledSecurity
```

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

### Quick Start Guide

#### Prerequisites
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
