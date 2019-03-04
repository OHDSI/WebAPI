# Basic Security Configuration

This tutorial will demonstrate how to configure the OHDSI WebAPI and ATLAS using the OHDSI WebAPI's built in SHIRO security configuration.  This configuration is intended for use in demonstration environments and is explicitly NOT for use in production.

## Assumptions
- This tutorial assumes that you already have a working version of the OHDSI WebAPI configured and running in your environment but with security disabled.
- This tutorial assumes that you already have a working version of ATLAS configured and running in your environment but with security disabled.

## settings.xml
The settings.xml file is used to configure your build of the OHDSI WebAPI in your development environment by allowing you to override the settings to the values in the settings.xml file.  You will need to make the following changes / additions to your settings.xml file in the profile you wish to use in this demonstration environment.

```xml
<security.provider>AtlasRegularSecurity</security.provider>
<security.origin>*</security.origin>
<security.db.datasource.url>jdbc:postgresql://localhost:5432/ohdsi</security.db.datasource.url>
<security.db.datasource.driverClassName>org.postgresql.Driver</security.db.datasource.driverClassName>
<security.db.datasource.schema>ohdsi</security.db.datasource.schema>
<security.db.datasource.username>ohdsi</security.db.datasource.username>
<security.db.datasource.password>ohdsi</security.db.datasource.password>
<security.db.datasource.authenticationQuery>select password from ${security.db.datasource.schema}.demo_security where email = ?</security.db.datasource.authenticationQuery>
```

## database 
Once you have completed the configuration of the profile for your OHDSI WebAPI you will need to create the table that will contain our sample login information.  The script to create a minimal sample table in a postgresql environment is as follows:

```sql
-- Table: ohdsi.demo_security

-- DROP TABLE ohdsi.demo_security;

CREATE TABLE ohdsi.demo_security
(
    email character varying(255) COLLATE pg_catalog."default",
    password character varying(255) COLLATE pg_catalog."default"
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE ohdsi.demo_security
    OWNER to ohdsi;
```

Next you will need to insert a sample record that will contain our demonstration username and password.  The password is encrypted using BCrypt.  You can create your own username and password or use the sample insert statement provided below where we have already encrypted the password 'ohdsi' for the user named 'ohdsi'.  To create a different password hash using BCrypt you can use the following web site:

https://www.dailycred.com/article/bcrypt-calculator

And then put that password hash into the statement below.

```sql
insert into ohdsi.demo_security (email,password) 
values ('ohdsi', '$2a$04$Fg8TEiD2u/xnDzaUQFyiP.uoDu4Do/tsYkTUCWNV0zTCW3HgnbJjO')
```

## Configuring ATLAS

Now that we have the OHDSI WebAPI configured, table created and populated we can now setup ATLAS to expect a secure OHDSI WebAPI.

Placing a `config-local.js` file inside the root `atlas/js` file in your web installation will allow you to override the configuration settings without requiring changes to the Github repository or accidentally pushing your local information to Github.  The following code configures ATLAS to expect a secure OHDSI WebAPI installation and configures it to use our newly created demonstration database.

```javascript
define([], function () {
	var configLocal = {};

	configLocal.api = {
		name: 'Demo Environment',
		url: 'http://localhost:8080/WebAPI/'
	};

	configLocal.userAuthenticationEnabled = true;

	configLocal.authProviders = [{
		"name": "Local Security Test DB",
		"url": "user/login/db",
		"ajax": true,
		"icon": "fa fa-database",
		"isUseCredentialsForm": true
	}];

	return configLocal;
});
```

### Becoming an Admin
You should now be able to load ATLAS and find that you can login to the environment using the newly created user and password information.  However, you will have limited permissions.  The following query will list the current permissions that your login has in the database:

```sql
select sec_user.id as user_id, login, sec_role.id as role_id, sec_role.name as role_name
from sec_user
join sec_user_role on sec_user.id = sec_user_role.user_id
join sec_role on sec_user_role.role_id = sec_role.id
```

To grant yourself administrator privileges you can run the following query:

```sql
insert into sec_user_role (user_id, role_id) values (1000,2)
```

Now by logging out and logging back in to ATLAS you should be granted administrative rights across the system.  You will then be able to manage other permissions from the `Manage permissions` section found in the configuration tab.

