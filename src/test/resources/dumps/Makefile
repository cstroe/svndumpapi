
setup:
	rm -fR svn-test
	mkdir -p svn-test/repos/testrepo
	mkdir -p svn-test/checkout
	cd svn-test/repos
	svnadmin create svn-test/repos/testrepo
	cd svn-test/checkout; svn checkout "file:///$(CURDIR)/svn-test/repos/testrepo" testrepo

empty: setup empty.dump
	svnadmin dump svn-test/repos/testrepo > empty.dump

firstcommit: setup firstcommit.dump
	touch svn-test/checkout/testrepo/firstFile.txt
	cd svn-test/checkout/testrepo; svn add firstFile.txt
	cd svn-test/checkout/testrepo; svn commit -m "Added a first file."
	svnadmin dump svn-test/repos/testrepo > firstcommit.dump
