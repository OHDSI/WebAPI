compile:
	mvn clean build -DskipUnitTests -DskipITtests -s WebAPIConfig/settings.xml -P webapi-postgresql

package: compile
	mvn clean package -DskipUnitTests -DskipITtests -s WebAPIConfig/settings.xml -P webapi-postgresql

deploy: package
	sudo /home/ubuntu/Downloads/apache-tomcat-8.5.84/bin/shutdown.sh 
	sudo mv /home/ubuntu/Downloads/apache-tomcat-8.5.84/webapps/WebAPI /tmp/WebAPI-FOLDER-`date +%m%d%H%S`
	sudo mv /home/ubuntu/Downloads/apache-tomcat-8.5.84/webapps/WebAPI.war /tmp/WebAPI.war-`date +%m%d%H%S`
	mv target/WebAPI.war /home/ubuntu/Downloads/apache-tomcat-8.5.84/webapps/
	sudo /home/ubuntu/Downloads/apache-tomcat-8.5.84/bin/startup.sh

git-push:
	git push

test:
	wget -O /tmp/tests/test-drug-rollup-branded-drug.json "http://api.ohdsi.org/WebAPI/CS1/evidence/drugrollup/brandeddrug/1000640"

test-public:
	wget -O /tmp/tests/test-drug-rollup-branded-drug.json "http://api.ohdsi.org/WebAPI/CS1/evidence/drugrollup/brandeddrug/1000640"
