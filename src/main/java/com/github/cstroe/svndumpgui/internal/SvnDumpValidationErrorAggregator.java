package com.github.cstroe.svndumpgui.internal;


import com.github.cstroe.svndumpgui.api.SvnDumpValidationError;
import com.github.cstroe.svndumpgui.api.SvnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class SvnDumpValidationErrorAggregator implements SvnDumpValidationError {

    private List<SvnDumpValidationError> errors = new ArrayList<>();

    public void add(SvnDumpValidationError error) {
        errors.add(error);
    }

    @Override
    public String getMessage() {
        return errors.stream().map(SvnDumpValidationError::getMessage).collect(Collectors.joining("\n\n"));
    }

    @Override
    public int getRevision() {
        OptionalInt min = errors.stream().mapToInt(SvnDumpValidationError::getRevision).min();
        if(min.isPresent()) {
            return min.getAsInt();
        }
        return -1;
    }

    @Override
    public SvnNode getNode() {
        throw new UnsupportedOperationException("Did not spec out what this should return.");
    }
}