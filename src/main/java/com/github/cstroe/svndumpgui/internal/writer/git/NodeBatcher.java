package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.Node;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.List;

public interface NodeBatcher {
    List<Pair<ChangeType, List<Triplet<String, String, Node>>>> batch(List<Quartet<ChangeType, String, String, Node>> nodes);
}
