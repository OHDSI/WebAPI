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

- **Java 21** (OpenJDK recommended - get from [Adoptium](https://adoptium.net/))
- **Maven 3.x** (check via `mvn -v`)
- **Docker** (check via `docker -v`) - for local PostgreSQL
- **PostgreSQL client** (optional - check via `psql --version`)

#### Installation & Running

**Option 1: Using Pre-built JAR (Recommended)**

1. **Download the latest release:**
   ```bash
   wget https://github.com/OHDSI/WebAPI/releases/latest/download/WebAPI.jar
   ```

2. **Set up PostgreSQL database:**
   ```bash
   docker create --name postgres-webapi -p 8432:5432 -e POSTGRES_PASSWORD=ohdsi postgres:15.0-alpine
   docker start postgres-webapi
   ```

3. **Run WebAPI:**
   ```bash
   java -jar WebAPI.jar --spring.profiles.active=webapi-postgresql
   ```

4. **Access the API:**
   - Open browser: http://localhost:8080/WebAPI/
   - Log in with any username
   - Grant admin privileges: `INSERT INTO sec_user_role (user_id, role_id, origin) VALUES (1000, 2, 'SYSTEM');`

**Option 2: Building from Source**

1. **Clone and build:**
   ```bash
   git clone https://github.com/OHDSI/WebAPI.git
   cd WebAPI
   export JAVA_HOME="/path/to/jdk-21"  # Adjust to your Java 21 installation
   mvn clean package -DskipTests
   ```

2. **Set up database (same as above):**
   ```bash
   docker create --name postgres-webapi -p 8432:5432 -e POSTGRES_PASSWORD=ohdsi postgres:15.0-alpine
   docker start postgres-webapi
   ```

3. **Run the JAR:**
   ```bash
   java -jar target/WebAPI.jar --spring.profiles.active=webapi-postgresql
   ```

**Option 3: Development Mode (Maven)**

```bash
mvn spring-boot:run -P webapi-postgresql
```

#### Configuration

**No recompilation needed!** Override configuration via:

**1. Command-line arguments:**
```bash
java -jar WebAPI.jar \
  --spring.profiles.active=webapi-postgresql \
  --datasource.url=jdbc:postgresql://localhost:8432/ohdsi \
  --datasource.username=postgres \
  --datasource.password=ohdsi
```

**2. External `application.properties` file** (in same directory as JAR):
```properties
spring.profiles.active=webapi-postgresql
datasource.url=jdbc:postgresql://localhost:8432/ohdsi
datasource.username=postgres
datasource.password=ohdsi
datasource.ohdsi.schema=webapi
```

**3. Environment variables:**
```bash
export DATASOURCE_URL=jdbc:postgresql://localhost:8432/ohdsi
export DATASOURCE_USERNAME=postgres
export DATASOURCE_PASSWORD=ohdsi
java -jar WebAPI.jar --spring.profiles.active=webapi-postgresql
```

#### Database Profiles

WebAPI supports multiple databases:

```bash
# PostgreSQL (recommended for development)
java -jar WebAPI.jar --spring.profiles.active=webapi-postgresql

# SQL Server
java -jar WebAPI.jar --spring.profiles.active=webapi-sqlserver

# Oracle
java -jar WebAPI.jar --spring.profiles.active=webapi-oracle
```

#### Configuration Properties

Common properties you can override:

| Property | Description | Example |
|----------|-------------|---------|
| `datasource.url` | Database JDBC URL | `jdbc:postgresql://localhost:5432/ohdsi` |
| `datasource.username` | Database username | `webapi_user` |
| `datasource.password` | Database password | `secret123` |
| `datasource.ohdsi.schema` | Schema for WebAPI tables | `webapi` |
| `security.provider` | Security provider | `DisabledSecurity` or `AtlasRegularSecurity` |
| `security.origin` | CORS origin | `http://localhost` |
| `flyway.datasource.url` | Flyway migration database | `jdbc:postgresql://localhost:5432/ohdsi` |

See `src/main/resources/application.properties` for all available properties.

#### Next Steps

At this point you have WebAPI running with an admin account. To use it effectively:

1. **Set up security** - Configure authentication providers (see Security section below)
2. **Add CDM database sources** - Connect to your OMOP CDM databases
3. **Configure permissions** - Set up user roles and access controls

See the [Installation Guide](https://github.com/OHDSI/WebAPI/wiki) for detailed setup instructions.

### Deployment Options

**Docker Container**

```bash
# Pull from Docker Hub (when available)
docker pull ohdsi/webapi:latest

# Or build from source
docker build -t webapi:local .

# Run container
docker run -d \
  --name webapi \
  -p 8080:8080 \
  -e DATASOURCE_URL=jdbc:postgresql://db-server:5432/ohdsi \
  -e DATASOURCE_USERNAME=webapi_user \
  -e DATASOURCE_PASSWORD=secret \
  ohdsi/webapi:latest
```

### Performance Tuning

**JVM Options:**

```bash
java -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/webapi/heap_dump.hprof \
  -jar WebAPI.jar
```

**Common JVM parameters:**
- `-Xms2g` - Initial heap size (2GB)
- `-Xmx4g` - Maximum heap size (4GB)
- `-XX:+UseG1GC` - Use G1 garbage collector (recommended for Spring Boot)
- `-XX:MaxGCPauseMillis=200` - Target max GC pause time

### Troubleshooting

**Check logs:**
```bash
# If running in Docker
docker logs -f webapi

# If running manually
java -jar WebAPI.jar > webapi.log 2>&1
```

**Common issues:**
- **Connection refused**: Check database connectivity and credentials
- **Port already in use**: Change port with `--server.port=8081`
- **Out of memory**: Increase heap size with `-Xmx` parameter
- **Slow startup**: Ensure adequate resources (2GB+ RAM recommended)

## License
OHDSI WebAPI is licensed under Apache License 2.0
