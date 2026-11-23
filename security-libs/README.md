# Java 8 security-maintained source dependencies

These small source forks remove Handlebars without dropping GIS or datasource
Kerberos support. They are built in the same reactor as WebAPI, not patched into
prebuilt JARs or installed manually in a developer's Maven cache.

```sh
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home
mvn -f pom-security.xml -B --no-transfer-progress package -Pwebapi-docker,webapi-databricks,webapi-gis
```

Run this from the WebAPI repository root. The normal application WAR is still
`target/WebAPI.war`. Use this reactor entry point instead of building only the
root `pom.xml`; the latter requires the fork artifacts to have been installed
separately. The permanent DockerEnv build script uses this reactor automatically.

## Provenance

- `common-utils` and `data-source-manager`: OHDSI/ArachneCommons tag `v1.17.4`,
  commit `5db47292077d4f016aece68295e3fabd48ad7bc1`.
- `atlasgis`: OHDSI/webapi-component-geospatial tag `v1.0.1`,
  commit `d3f2ac7619044138d30a1a4c2d3778c62eace368`.

Original copyright/license notices are retained. The maintained artifacts have
explicit `-webapi1` versions and retain Maven package metadata for scanning.

The scalar renderer supports only the `{{name}}` substitutions actually used by
these versions. It does not evaluate expressions, access bean properties, load
arbitrary filesystem files, or recursively expand replacement text. R scripts and all non-template
content remain unchanged. GIS and datasource-manager are recompiled against the
new owned template API to avoid binary-incompatible calls to Handlebars types.

## Removed directory features

This branch intentionally no longer supports LDAP/AD login or directory-based
user imports. Existing entities, history and schema migrations remain intact.
Remove any deployment overrides enabling `security.auth.ldap.enabled` or
`security.auth.ad.enabled`; both must be false. Startup fails clearly if either
is enabled or enabled, non-closed directory-import jobs remain in the database.
No database records are automatically disabled or deleted.
