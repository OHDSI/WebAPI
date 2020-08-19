./mvnw clean install -s settings.xml -Pcentral -DskipTests

docker build --no-cache -t honeur/webapi .