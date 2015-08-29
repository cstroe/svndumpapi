package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.*;

import java.util.HashMap;
import java.util.Map;

public class PathCollision implements SvnDumpValidator {
    private String message = "";

    @Override
    public boolean isValid(SvnDump dump) {
        Map<String, Integer> pathByRevision = new HashMap<>();
        for(SvnRevision revision : dump.getRevisions()) {
            for(SvnNode node : revision.getNodes()) {
                String action = node.get(SvnNodeHeader.ACTION);
                String path = node.get(SvnNodeHeader.PATH);
                if("add".equals(action)) {
                    if(pathByRevision.containsKey(path)) {
                        message = "Error at revision " + revision.getNumber() + "\n" +
                                "adding " + path + "\n" +
                                "but it was already added in revision " + pathByRevision.get(path);
                        return false;
                    } else {
                        pathByRevision.put(path, revision.getNumber());
                    }
                } else if("delete".equals(action)) {
                    if(!pathByRevision.containsKey(path)) {
                        message = "Error at revision " + revision.getNumber() + "\n" +
                                "deleting " + path + "\n" +
                                "but it does not exist";
                        return false;
                    } else {
                        pathByRevision.remove(path);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
