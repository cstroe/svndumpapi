package com.github.cstroe.svndumpgui.api;

public interface SvnProperty {
    String DATE = "svn:date";
    String AUTHOR = "svn:author";
    String LOG = "svn:log";
    String MIMETYPE = "svn:mime-type";
    String MERGEINFO = "svn:mergeinfo";

    /**
     * Used in {@link com.github.cstroe.svndumpgui.generated.SvnDumpFileParser#Start(SvnDumpConsumer)}
     * to keep track of how many EOLs are actually in the dump file, so that
     * {@link com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriterImpl#endNode(SvnNode)}
     * can produce the matching number of new lines.
     *
     * Had to resort to this "hack" because the SVN dump file description is not specific enough
     * and it was too hard to decypher the behavior of "svnadmin dump" with regards to new lines.
     */
    String HACK_TRAILING_NEWLINE = "HACK_TRAILING_NEWLINE";
}
