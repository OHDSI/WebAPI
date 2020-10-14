To build WebAPI with Impala support do the following:
1. Go to the https://www.cloudera.com/downloads/connectors/impala/jdbc/2-6-15.html
2. Register to Cloudera if you did not registered earlier or sign in to your Cloudera account
3. Download the latest Impala JDBC drivers
4. Unpack archive and set the impala.classpath property in your settings.xml to the unpacked archive location (ie: C://downloads/impalaJDBC) inside the webapi-impala profile.
5. Build WebAPI with webapi-impala profile. 
   * mvn -Pwebapi-postgresql,webapi-impala clean package
