.PHONY: package run-stdlib-tests stdlib-tests test

package:
	mvn package -Dmaven.test.skip=true

stdlib-tests: package run-stdlib-tests

run-stdlib-tests:
	./hod stdlib StdlibTests.Main --backend=javascript
	./hod stdlib StdlibTests.Main --backend=python
	./hod stdlib StdlibTests.Main

test: stdlib-tests
	mvn test
