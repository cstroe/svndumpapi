package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpParser;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.io.*;

public class SvnDumpParserImpl implements SvnDumpParser {

    private SvnRevision currentRevision;
    private SvnDumpImpl svnDump;

    @Override
    public SvnDump parse(InputStream is) throws IOException {
        SvnDumpImpl dump = new SvnDumpImpl();

        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(reader);


        String line = br.readLine();

        int colonIndex = line.indexOf(':');

        String varName = line.substring(1, colonIndex);

        switch(varName) {
            case "Revision-number": {
                if(currentRevision != null) {
                    svnDump.addRevision(currentRevision);
                    currentRevision = null;
                }
                String revNumberRaw = line.substring(colonIndex);
                Integer.parseInt(revNumberRaw);
            }
            case "UUID":
            case "SVN-fs-dump-format-version":
            default:
                break;
        }

        return new SvnDumpImpl();
    }
}
