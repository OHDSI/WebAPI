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

## Managing auth providers

The following parameters are used to enable/disable certain provider:

- `security.auth.windows.enabled`
- `security.auth.kerberos.enabled`
- `security.auth.openid.enabled`
- `security.auth.facebook.enabled`
- `security.auth.github.enabled`
- `security.auth.google.enabled`
- `security.auth.jdbc.enabled`
- `security.auth.ldap.enabled`
- `security.auth.ad.enabled`
- `security.auth.cas.enabled`

Acceptable values are `true` and `false`

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