# Oracle Setup Guide

### Database Engine Installation
You can install the Oracle 11g XE core distribution from the following URL: 
http://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.html
Follow your platform specific installation to install the binary and start the service.  Remember the db admin user and password that is set during the installation process since this will be needed later to administer the server (creating new databases and adding new schema).

### Oracle database administration
For the database administration, we will be using the Oracle SQL Developer tool available from the Oracle Downloads URL above (found under the 'other downloads' links at the bottom of the page).

### Creating the database Users and Roles
If your environment already has application level administration and non-administrative access groups created, you can skip this section on creating the roles.

The oracle installation will contain a database instance, and roles and users should be created for administering the OHDSI database and for non-administrative access (ie: application access) to the database.  In this example, however, we’ll be creating one user: ohdsi that will provide the schema that will host the WebAPI generated tables.  In more advanced cases, you may have a special administrator account for applications that has certain rights on the databases to create tables, but for simplicity, we’ll be using the schema user to administer the tables and also serve as the application user context for the normal WebAPI operations.

### Creating Database Group Roles
For this example, we will not be creating any special database groups.  We will use the schema user as the administrative account and runtime database user. This is not recommended for production environments: a separate admin and runtime user should be defined and granted the appropriate permissions for their roles.

### Creating the OHDSI schema
In Oracle, schemas operate in a slightly different way than other database platforms.  For purpose of this guide, we’ll be implementing a new schema as a user, but there are other tools (such as RCU) which can be used to create new schemas owned by existing users, but those tools are beyond the scope of this guide.  In this simple setup, we will create a user 'ohdsi' which will be used as the administrative account for WebAPI and all tables will be created under this user's schema.

To create a user, expand the SYSTEM connection in oracle SQL developer, and navigate to Other Users.  Right-click Other Users, and click ‘Create User’.  

#### Tab: Granted Roles ####

Login: ohdsi
Role Name: CONNECT, RESOURCE

#### Tab: System Privileges ####

Privilege: UNLIMITED TABLESPACE

After these values are set, the generated SQL to create this role is produced:

```sql
-- USER SQL
CREATE USER ohdsi IDENTIFIED BY {password}
DEFAULT TABLESPACE "SYSTEM"
TEMPORARY TABLESPACE "TEMP";

-- QUOTAS

-- ROLES
GRANT "CONNECT" TO ohdsi ;
GRANT "RESOURCE" TO ohdsi ;
ALTER USER ohdsi DEFAULT ROLE "CONNECT","RESOURCE";

-- SYSTEM PRIVILEGES
GRANT UNLIMITED TABLESPACE TO ohdsi ;
```

### Prepare Schema for WebAPI
In this simple example, no special additional steps are required to prepare the schema.  The user is granted all rights to create the tables under their own schema.  However, permissions will be required to be granted to query the CDM tables (in this simple case, the CDM tables will be located under another user on the same Oracle instance).  It is very important that the OHDSI tables are co-located with the CDM tables.  In more advanced scenarios, this is not a hard restriction, but will require additional steps to configure the environment this way.  The benefit of this complexity is the ability to execute OHSI analysis across multiple CDMs.
