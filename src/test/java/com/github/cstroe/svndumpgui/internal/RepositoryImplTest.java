package com.github.cstroe.svndumpgui.internal;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class RepositoryImplTest {

    private static final int svnRevisionNumber = 1234;

    @Test
    public void add_revision() {
        RepositoryImpl dump = new RepositoryImpl();

        assertThat(dump.getRevisions().size(), is(0));

        dump.addRevision(new RevisionImpl(svnRevisionNumber));

        assertThat(dump.getRevisions().size(), is(1));
        assertThat(dump.getRevisions().get(0).getNumber(), is(svnRevisionNumber));
    }

}