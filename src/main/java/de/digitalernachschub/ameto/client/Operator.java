package de.digitalernachschub.ameto.client;

import lombok.Value;

import java.util.List;

/**
 * Describes a processing step in a pipeline.
 */
@Value
public class Operator {
    String name;
    List<String> consumes;
}
