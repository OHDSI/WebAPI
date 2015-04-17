compile:
	mvn clean
	mvn compile -Pwebapi-postgresql

package: compile
	mvn package -Pwebapi-postgresql

deploy: package 
	sudo service tomcat7 stop
	sleep 4
	sudo rm -rf /var/lib/tomcat7/webapps/WebAPI*
	sudo cp -r target/WebAPI.war  /var/lib/tomcat7/webapps/
	sudo chown tomcat7 /var/lib/tomcat7/webapps/WebAPI.war
	sudo chgrp tomcat7 /var/lib/tomcat7/webapps/WebAPI.war
	sudo service tomcat7 start

git-push:
	git push myfork master

test:
	wget -O tests/test-drug-hoi.json "http://localhost:8080/WebAPI/evidence/drughoi/1000640-137682"
	wget -O tests/test-drug.json "http://localhost:8080/WebAPI/evidence/drug/1000640"
	wget -O tests/test-hoi.json "http://localhost:8080/WebAPI/evidence/hoi/320073"
	wget -O tests/test-info.json "http://localhost:8080/WebAPI/evidence/info"
	wget -O tests/test-drug-hoi-eu-spc.json "http://localhost:8080/WebAPI/evidence/drughoi/40239056-75053" 
	wget -O tests/test-drug-hoi-splicer.json "http://localhost:8080/WebAPI/evidence/drughoi/19133853-195588"
	wget -O tests/test-drug-hoi-faers-counts-and-signals.json "http://localhost:8080/WebAPI/evidence/drughoi/1308216-436681"
	wget -O tests/test-drug-hoi-pubmed-mesh-cr.json "http://localhost:8080/WebAPI/evidence/drughoi/1154343-433031" 
	wget -O tests/test-drug-hoi-pubmed-mesh-clin-trial.json "http://localhost:8080/WebAPI/evidence/drughoi/789578-378144"
	wget -O tests/test-drug-hoi-pubmed-mesh-other.json "http://localhost:8080/WebAPI/evidence/drughoi/19010482-316866"
	wget -O tests/test-drug-hoi-semmed-cr.json "http://localhost:8080/WebAPI/evidence/drughoi/1782521-45612000"
	wget -O tests/test-drug-hoi-semmed-clin-trial.json "http://localhost:8080/WebAPI/evidence/drughoi/1303425-45616736"
	wget -O tests/test-drug-rollup-ingredient.json "http://localhost:8080/WebAPI/evidence/drugrollup/ingredient/1000632"
	wget -O tests/test-drug-rollup-clin-drug.json "http://localhost:8080/WebAPI/evidence/drugrollup/clinicaldrug/19074181"
	wget -O tests/test-drug-rollup-branded-drug.json "http://localhost:8080/WebAPI/evidence/drugrollup/brandeddrug/1000640"
