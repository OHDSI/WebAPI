To build WebAPI with Apache Hive LLAP support do the following:
1. Add to your hosts line: "<your sandbox ip> sandbox-hdp.hortonworks.com"
2. Download the JDBC driver from http://sandbox-hdp.hortonworks.com:10002/jdbcjar
3. Set the hive.classpath property in your settings.xml to the archive location (ie: C://downloads/hive) inside the webapi-hive profile
4. Build WebAPI with webapi-hive profile:
   * mvn -Pwebapi-hive clean package