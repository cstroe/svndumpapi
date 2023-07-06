package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.Node;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;

public class NodeBatcherImpl implements NodeBatcher {
    @Override
    public List<Pair<ChangeType, List<Triplet<String, String, Node>>>> batch(List<Quartet<ChangeType, String, String, Node>> nodes) {
        List<Pair<ChangeType, List<Triplet<String, String, Node>>>> batches = new ArrayList<>();

        List<Triplet<String, String, Node>> currentBatch = new ArrayList<>();
        ChangeType currentChangeType = null;
        for (Quartet<ChangeType, String, String, Node> quartet : nodes) {
            if (currentChangeType == null) {
                currentChangeType = quartet.getValue0();
                currentBatch.add(Triplet.with(quartet.getValue1(), quartet.getValue2(), quartet.getValue3()));
            } else if (currentChangeType != quartet.getValue0()) {
                batches.add(Pair.with(currentChangeType, currentBatch));
                currentChangeType = quartet.getValue0();
                currentBatch = new ArrayList<>();
                currentBatch.add(Triplet.with(quartet.getValue1(), quartet.getValue2(), quartet.getValue3()));
            } else {
                currentBatch.add(Triplet.with(quartet.getValue1(), quartet.getValue2(), quartet.getValue3()));
            }
        }

        if (!currentBatch.isEmpty()) {
            batches.add(Pair.with(currentChangeType, currentBatch));
        }

        return batches;
    }
}
