package com.github.cstroe.svndumpgui.api;

public interface Property {
    String DATE = "svn:date";
    String AUTHOR = "svn:author";
    String LOG = "svn:log";
    String IGNORE = "svn:ignore";
    String MIMETYPE = "svn:mime-type";
    String MERGEINFO = "svn:mergeinfo";

    /**
     * Used in {@link com.github.cstroe.svndumpgui.generated.SvnDumpFileParser#Start(RepositoryConsumer)}
     * to keep track of how many EOLs are actually in the dump file, so that
     * {@link com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriter#endNode(Node)}
     * can produce the matching number of new lines.
     *
     * Had to resort to this "hack" because the SVN dump file description is not specific enough
     * and it was too hard to decipher the behavior of "svnadmin dump" with regards to new lines.
     */
    String TRAILING_NEWLINE_HINT = "TRAILING_NEWLINE_HINT";
}
