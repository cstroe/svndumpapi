package com.github.cstroe.svndumpgui.cli;

import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.writer.AbstractRepositoryWriter;
import com.github.cstroe.svndumpgui.internal.writer.RepositorySummary;
import com.github.cstroe.svndumpgui.internal.writer.git.GitWriter;
import com.github.cstroe.svndumpgui.internal.writer.git.GitWriterNoBranching;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;

public class CliConsumer {
    public static void main(final String[] args) throws ParseException, UnsupportedEncodingException, FileNotFoundException {
        FileInputStream fis = new FileInputStream("/home/cosmin/Zoo/freshports/fp.svndump");
        AbstractRepositoryWriter gitWriter;
        try {
//            gitWriter = new GitWriter(743, "/tmpfs/svndumpadmin-git-3448828562389816950");
            gitWriter = new GitWriterNoBranching("svndumpadmin-git-5483822688254000915", 5068);
        } catch (GitAPIException | IOException ex) {
            ex.printStackTrace(System.err);
            return;
        }
        RepositorySummary summary = new RepositorySummary();
        summary.continueTo(gitWriter);
        SvnDumpParser.consume(fis, summary);
        System.out.flush();
        System.err.flush();
    }
}
