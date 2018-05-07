To build WebAPI with Impala support do the following:
1. Go to the https://www.cloudera.com/downloads/connectors/impala/jdbc/2-5-43.html
1. Register to clouder if you did not registered earlier or sign in to your Cloudera account
1. Download the latest Impala JDBC drivers
1. Unpack archive and copy all jars from directory with name looks like ClouderaImpalaJDBC41 here.
1. Build WebAPI with webapi-impala profile. 
   * mvn -Pwebapi-postgresql,webapi-impala clean package

