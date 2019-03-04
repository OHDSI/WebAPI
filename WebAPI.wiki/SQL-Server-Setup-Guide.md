# SQL Server Setup Guide #

**DISCLAIMER:** This installation guide is aimed at providing a basic setup for SQL Server to get up-and-running with OHDSI's WebAPI and other tools. Please follow Microsoft's recommendations if setting this up for production use.

## Database Engine Installation ##

You can install a free version of SQL Server Express from the following URL: 
https://www.microsoft.com/en-us/sql-server/sql-server-editions-express
Follow your platform specific installation to install and start the database service.  Remember the SQL Server user and password that is set during the installation process since this will be needed later to administer the server.

## SQL Server database administration ##

For the database administration, we will be using the SQL Server Management Studio which is bundled with the SQL Server installation. After launching, you will be prompted to log into your server with the credentials that you set up during the installation.

### Creating the OHDSI WebAPI database ###

In SQL Server Management Studio, open the Databases folder. Right click the Databases folder and select ‘New Database’. In the Database name field, enter "OHDSI" and leave all of the other defaults. Here is a good resource if you are interested in other database settings/options: <a href="https://msdn.microsoft.com/en-us/library/ms190249(v=sql.105).aspx">Setting Database Options</a>

### Creating the database user and setting security ###
Next we'll create a user with non-administrative access (ie: application access) to the OHDSI database. Using SQL Server Management Studio, expand the Security folder under the server (not under the OHDSI database). Next, right click on the Logins folder and select "New Login".

The "General" page will be shown which will allow you to specify the details for the user. Here are the settings to use:

- Login Name: ohdsi_app_user
- Select the "SQL Server authentication" radio button to enable the fields below it.
- In the Password and Confirm Password boxes, enter "app1" (no quotes)
- Uncheck all three options: "Enforce password policy", "Enforce password expiration" and "User must change password at next login"
- In the "Default database" drop down, select "OHDSI" from the list.

Next, under the "Select a page" section on the top-left, select the "User Mapping" option. On the right, you will see list of databases under the heading "Users mapped to this login". Find the OHDSI database in this list, place a check mark next to it and then click on the row to select the database. In the section below the database list will be another checklist labeled "Database role membership for OHDSI". From this list, check "db_owner" and leave the "public" role checked as well.

Hit OK to save this information. In this process, we've created a user named "ohdsi_app_user" with a password of "app1" which you will then use when configuration your WebAPI installation. By providing this user with the "db_owner" role, this login will have the right to manage data and tables within the OHDSI database. This is **not** a recommended approach for a production environment.
