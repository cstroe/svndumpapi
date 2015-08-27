all: clean generate

generate: svndump.jj
	javacc svndump.jj

compile:
	javac *.java

clean:
	rm -f *.java *.class

test:
	java SvnDumpFileParser < test.dump
