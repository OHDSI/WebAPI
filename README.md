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

## Support

- Developer questions/comments/feedback: [OHDSI forum](http://forums.ohdsi.org/c/developers)
- We use the [GitHub issue tracker](https://github.com/OHDSI/WebAPI/issues) for all bugs/issues/enhancements.

## License
OHDSI WebAPI is licensed under Apache License 2.0

## Kerberos
Used to initialize kerberos support in active directory domain. Case is sensitive for capitalized words

Active directory admins have to make the following:
- Create account for application (webapi)

domain\<Application title (arbitrary)>

Example

domain\Atlas_acc

- Create spn

C:\Windows\system32>setspn -A HTTP/<Tomcat server domain name> <Application title (arbitrary)>

Example

C:\Windows\system32>setspn -A HTTP/ssuvorov.domain.corp atlas_acc

C:\Windows\system32>setspn -L <Application title>

Example

C:\Windows\system32>setspn -L atlas_acc

- Create keytab file

C:\Windows\system32>ktpass -princ HTTP/<Tomcat server domain name>@<Realm> -mapuser <Application title> -pass <Application password (from application account)> -ptype KRB5_NT_PRINCIPAL -out <Path to keytab file>

Example

C:\Windows\system32>ktpass -princ HTTP/ssuvorov.domain.corp@domain.corp -mapuser atlas_acc -pass UjRxS3hU -ptype KRB5_NT_PRINCIPAL -out C:\atlas.keytab

- Create application properties (webapi)

security.kerberos.keytabPath - Absolute path to keytab file

security.kerberos.spn - HTTP/<Tomcat server domain name>@<Realm>

Example

security.kerberos.keytabPath=c://tmp//atlas.keytab

security.kerberos.spn=HTTP/ssuvorov.domain.corp@DOMAIN.CORP



