package de.ameto.client.operators;

import java.util.Collections;
import java.util.List;

/**
 * Describes a processing step in a pipeline.
 */
public interface Operator {
    String getName();
    String getVersion();
    List<String> getConsumes();

    default List<String> getArguments() {
        return Collections.emptyList();
    }
}
