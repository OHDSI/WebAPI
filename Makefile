compile:
	mvn clean compile -DskipUnitTests -DskipITtests -s WebAPIConfig/settings.xml -P webapi-postgresql

package: compile
	mvn package -DskipUnitTests -DskipITtests -s WebAPIConfig/settings.xml -P webapi-postgresql

deploy: package
	/home/ubuntu/Downloads/apache-tomcat-8.5.84-DEV/bin/shutdown.sh 
	mv /home/ubuntu/Downloads/apache-tomcat-8.5.84-DEV/webapps/WebAPI /mnt/disk1/webapi-dev-tmp/WebAPI-FOLDER-`date +%m%d%H%S`
	mv /home/ubuntu/Downloads/apache-tomcat-8.5.84-DEV/webapps/WebAPI.war /mnt/disk1/webapi-dev-tmp/WebAPI.war-`date +%m%d%H%S`
	mv target/WebAPI.war /home/ubuntu/Downloads/apache-tomcat-8.5.84-DEV/webapps/
	echo "Now run /home/ubuntu/Downloads/apache-tomcat-8.5.84-DEV/bin/startup.sh"

git-push:
	git push

test:
	wget -O /tmp/tests/test-drug-rollup-branded-drug.json "http://api.ohdsi.org/WebAPI/CS1/evidence/drugrollup/brandeddrug/1000640"

test-public:
	wget -O /tmp/tests/test-drug-rollup-branded-drug.json "http://api.ohdsi.org/WebAPI/CS1/evidence/drugrollup/brandeddrug/1000640"
