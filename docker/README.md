1. Create SSH keys in the same folder. Follow instructions here:
   https://support.atlassian.com/bitbucket-cloud/docs/set-up-an-ssh-key/
   
2. Add public key in Bitbucket:
   https://bitbucket.org/account/settings/ssh-keys/

3. Run command to build Docker image
   ```
   docker build -t atlas:2.9.0-plus .
   ```
   
4. After image is prepared you can run it like this
```dockerfile
docker create \
--restart=always \
--name=atlas \
-p 8080:8080 \
-v $(pwd)/config-local.js:/usr/local/tomcat/webapps/ROOT/js/config-local.js \
--env-file /etc/atlas/env-var.env \
atlas:2.9.0-plus && docker start atlas
```

For this you can create `config-local.js` file to overwrite settings for Atlas.
To customize WebAPI settings use `env-var.env` file. For example, to define PostgreSQL system database connection details:
```dockerfile
datasource_driverClassName=org.postgresql.Driver
datasource_url=jdbc:postgresql://server.com:5432/webapi
datasource_username=<username>
datasource_password=<password>
datasource_dialect=postgresql
datasource_ohdsi_schema=webapi
spring_jpa_properties_hibernate_default_schema=webapi
spring_batch_repository_tableprefix=webapi.BATCH_
flyway_placeholders_ohdsiSchema=webapi
flyway_datasource_driverClassName=org.postgresql.Driver
flyway_datasource_url=jdbc:postgresql://server.com:5432/webapi
flyway_datasource_username=<username>
flyway_datasource_password=<password>
flyway_schemas=webapi
flyway_locations=classpath:db/migration/postgresql
hibernate_dialect=org.hibernate.dialect.PostgreSQL9Dialect
```
Here we use the same parameters as in `pom.xml` file but with underscore sign instead of dot.