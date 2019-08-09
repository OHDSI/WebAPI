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



