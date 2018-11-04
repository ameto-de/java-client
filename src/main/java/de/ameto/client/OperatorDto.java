package de.ameto.client;

import lombok.Value;

import java.util.List;

/**
 * Describes a processing step in a pipeline.
 */
@Value
class OperatorDto {
    String name;
    List<String> consumes;
}
