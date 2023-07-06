package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.internal.utility.Tuple2;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.List;

public interface NodeSeparator {
    List<Quartet<ChangeType, String, String, Node>> separate(List<Node> nodes);
}
