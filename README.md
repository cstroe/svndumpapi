# SVN Dump GUI

Attempt to create a user interface for editing SVN dump files.

## SVNDumpFileParser

The `SvnDumpFileParser` is an auto-generated parser for SVN dump files 
(files created with `svnadmin dump`).  It will
parse SVN dump files into an `SvnDump` object.  The `SvnDump` representation is
meant to be very light-weight and does minimal validation.

The parser is auto-generated using JavaCC from the `svndump.jj` gramar file.
This grammar generates a parser that is dependenent on the Java interfaces and 
classes in this project.

### Usage

Parsing an SVN dump file is straight forward:

    InputStream s = new FileInputStream("svn_dump_file");
    SvnDumpFileParser parser = new SvnDumpFileParser(s, "ISO-8859-1");
    SvnDump dump = parser.Start();

See `SvnDumpFileParserTest` for usage patterns of the parser.
