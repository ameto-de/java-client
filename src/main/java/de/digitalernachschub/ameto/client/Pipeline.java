package de.digitalernachschub.ameto.client;

import lombok.Value;

import java.util.List;

/**
 * Represents a processing pipeline.
 * A pipeline consists of a unique name and an ordered list of processing steps, the operators.
 */
@Value
public class Pipeline {
    String name;
    List<Operator> operators;
}
