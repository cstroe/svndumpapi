all: clean adder

svndump: svndump.jj
	javacc svndump.jj
	javac *.java

clean:
	rm -f *.java *.class

test:
	java SvnDumpFileParser < test.dump
