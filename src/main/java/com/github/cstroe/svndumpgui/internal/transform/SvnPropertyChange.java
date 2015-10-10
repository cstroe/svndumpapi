package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class SvnPropertyChange extends AbstractSvnDumpMutator {

    private final Predicate<String> nameMatcher;
    private final Function<String, String> transform;

    public SvnPropertyChange(Predicate<String> nameMatcher, Function<String, String> transform) {
        this.nameMatcher = Optional.of(nameMatcher).get();
        this.transform = Optional.of(transform).get();
    }

    @Override
    public void consume(SvnNode node) {
        LinkedHashMap<String, String> newProperties = new LinkedHashMap<>();
        for(Map.Entry<String, String> entry : node.getProperties().entrySet()) {
            if(nameMatcher.test(entry.getKey())) {
                String newValue = transform.apply(entry.getValue());
                if(newValue != null) {
                    newProperties.put(entry.getKey(), newValue);
                }
            } else {
                newProperties.put(entry.getKey(), entry.getValue());
            }
        }

        node.setProperties(newProperties);
        super.consume(node);
    }
}
