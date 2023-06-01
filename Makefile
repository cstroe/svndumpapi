simple:
	mvn -Dmaven.test.skip=true clean install dependency:copy-dependencies
	cat ~/Zoo/freshports/fp.svndump | ./bin/run-java
