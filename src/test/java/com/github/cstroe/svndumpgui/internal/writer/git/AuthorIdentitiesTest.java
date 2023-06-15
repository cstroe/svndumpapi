package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.internal.RevisionImpl;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.Test;

import java.time.format.TextStyle;
import java.util.Locale;

import static org.junit.Assert.*;

public class AuthorIdentitiesTest {
    @Test
    public void returnsIdentity() {
        AuthorIdentities identities = new AuthorIdentities()
                .add("user", "user name", "username@example.com");

        Revision revision = new RevisionImpl(0, "2015-10-27T13:32:31.073005Z");
        revision.getProperties().put("svn:author", "user");

        PersonIdent ident = identities.from(revision);
        assertEquals("user name", ident.getName());
        assertEquals("username@example.com", ident.getEmailAddress());
        assertEquals("GMT", ident.getTimeZone().toZoneId().getDisplayName(TextStyle.SHORT, Locale.US));
        assertEquals("2015-10-27T13:32:31.073Z", ident.getWhenAsInstant().toString());
    }
}