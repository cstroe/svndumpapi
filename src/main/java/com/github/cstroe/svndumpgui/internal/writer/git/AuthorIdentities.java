package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.internal.utility.Tuple2;
import org.eclipse.jgit.lib.PersonIdent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class AuthorIdentities {
    private final Map<String, Tuple2<String, String>> identities = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX");
    private final Tuple2<String, String> defaultIdentity;

    public AuthorIdentities() {
        this.defaultIdentity = null;
    }

    public AuthorIdentities(Tuple2<String, String> defaultIdentity) {
        this.defaultIdentity = defaultIdentity;
    }

    public AuthorIdentities add(String svnName, String gitName, String email) {
        if (identities.containsKey(svnName)) {
            throw new RuntimeException("identites already exists");
        }

        identities.put(svnName, Tuple2.of(gitName, email));
        return this;
    }

    public PersonIdent from(Revision revision) {
        String author = revision.getAuthor();
        Tuple2<String, String> identity = identities.get(author);
        if (author == null) {
            if (defaultIdentity != null) {
                identity = defaultIdentity;
            } else {
                throw new RuntimeException(String.format("Revision '%d' does not have an author and no default identity has been provided.", revision.getNumber()));
            }
        }
        if (identity == null) {
            if (defaultIdentity != null) {
                identity = defaultIdentity;
            } else {
                throw new RuntimeException(String.format("Could not find an identity for SVN author '%s' and no default identity was provided.", author));
            }
        }

        String date = revision.getProperties().get("svn:date");
        Instant instant = OffsetDateTime.parse(date).toInstant();
        return new PersonIdent(
                identity._1,
                identity._2,
                instant,
                ZoneId.of("UTC"));
    }
}
