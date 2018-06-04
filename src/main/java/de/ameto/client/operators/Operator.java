package de.ameto.client.operators;

import java.util.List;

/**
 * Describes a processing step in a pipeline.
 */
public interface Operator {
    String getName();
    List<String> getConsumes();
}
