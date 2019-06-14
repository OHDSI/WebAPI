To build WebAPI with Google BigQuery support do the following:
1. Go to https://cloud.google.com/bigquery/partners/simba-drivers/ and 
2. Download the JDBC 4.2 driver
3. Unpack archive and set the bigquery.classpath property in your settings.xml to the unpacked archive location (ie: C://downloads/bigquery) inside the webapi-bigquery profile.
4. Build WebAPI with webapi-bigquery profile. 
   * mvn -Pwebapi-postgresql,webapi-bigquery clean package
