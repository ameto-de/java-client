package de.digitalernachschub.ameto.client;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.List;

/**
 * Represents a processing pipeline.
 * A pipeline consists of a unique name and an ordered list of processing steps, the operators.
 */
@Value
public class PipelineDto {
    String name;
    List<Step> steps;

    @Value
    @RequiredArgsConstructor
    public static class Step {
        String operator;
        List<String> arguments;

        public Step(String operator) {
            this.operator = operator;
            arguments = Collections.emptyList();
        }
    }
}
