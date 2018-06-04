package de.ameto.client;

import lombok.Value;

import java.util.List;

/**
 * Describes a processing step in a pipeline.
 */
@Value
public class OperatorDto {
    String name;
    List<String> consumes;
}
