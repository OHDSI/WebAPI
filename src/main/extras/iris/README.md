# Using WebAPI with InterSystems IRIS support

## Building WebAPI

To build WebAPI with InterSystems IRIS support, you can simply use the `webapi-iris` profile
when building WebAPI with Maven:

```Shell
mvn -Pwebapi-postgresql,webapi-iris clean package
```

or on Windows PowerShell:
```Shell
mvn "-Pwebapi-postgresql,webapi-iris" clean package
```

This will automatically download and package a recent version of the InterSystems IRIS JDBC
driver from Maven Central.

Alternatively, the JDBC driver for InterSystems IRIS can be found in the `dev/java/lib/1.8/`
subfolder of your IRIS installation, or as a download from InterSystems Cloud Services portal.


## Sample configuration

When configuring Atlas / WebAPI to access CDM on IRIS over a secure connection, your source 
registration looks like this:
```SQL
INSERT INTO webapi.source (source_id, source_name, source_key, source_connection, source_dialect, username, password)
  VALUES (<#>, 'OHDSI IRIS Database', 'IRIS', 'jdbc:IRIS://<hostname>.elb.us-west-2.amazonaws.com:443/USER/:::true', 
  'iris', '<username>','<password>');
```

For a non-encrypted, local connection, this may look like:
```SQL
INSERT INTO webapi.source (source_id, source_name, source_key, source_connection, source_dialect, username, password)
  VALUES (<#>, 'OHDSI IRIS Database', 'IRIS', 'jdbc:IRIS://localhost:1972/USER/', 'iris', '_SYSTEM_','SYS');
```


## References

For more information on how to connect to InterSystems IRIS or InterSystems IRIS Cloud SQL, please see:
* [Connecting your applications to Cloud SQL](https://docs.intersystems.com/components/csp/docbook/DocBook.UI.Page.cls?KEY=GDRIVE_cloudsql)
* [Troubleshooting connection issues](https://docs.intersystems.com/services/csp/docbook/DocBook.UI.Page.cls?KEY=ACTS)
* [InterSystems fork for OHDSI Broadsea](https://github.com/isc-krakshith/InterSystems-Broadsea/)