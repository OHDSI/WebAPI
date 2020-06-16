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

OHDSI WebAPI is a Java 8 web application that utilizes a database (PostgreSQL, SQL Server or Oracle) for storage.

## System Requirements & Installation

Documentation can be found a the [Web API Installation Guide](https://github.com/OHDSI/WebAPI/wiki) which covers the system requirements and installation instructions.

## SAML Auth support
The following parameters are used:
security.saml.idpMetadataLocation=classpath:saml/dev/idp-metadata.xml- path to metadata used by identity provider
security.saml.metadataLocation=saml/dev/sp-metadata.xml - service provider metedata path
security.saml.keyManager.keyStoreFile=classpath:saml/samlKeystore.jks - path to keystore
security.saml.keyManager.storePassword=nalle123 - keystore password
security.saml.keyManager.passwords.arachnenetwork=nalle123 - private key password
security.saml.keyManager.defaultKey=apollo - keystore alias
security.saml.sloUrl=https://localhost:8443/cas/logout - identity provider logout URL
security.saml.callbackUrl=http://localhost:8080/WebAPI/user/saml/callback - URL called from identity provider after login

## Testing

It was chosen to use embedded PG instead of H2 for unit tests since H2 doesn't support window functions, `md5` function, HEX to BIT conversion, `setval`, `set datestyle`, CTAS + CTE.

## Support

- Developer questions/comments/feedback: [OHDSI forum](http://forums.ohdsi.org/c/developers)
- We use the [GitHub issue tracker](https://github.com/OHDSI/WebAPI/issues) for all bugs/issues/enhancements.

## Contribution

### Versioning
- WebAPI follows [Semantic versioning](https://semver.org/);
- Only Non-SNAPSHOT dependencies should be presented in POM.xml on release branches/tags.

## License
OHDSI WebAPI is licensed under Apache License 2.0


