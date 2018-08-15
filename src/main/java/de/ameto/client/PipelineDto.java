package de.ameto.client;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.List;

/**
 * Represents a processing pipeline.
 * A pipeline consists of a unique name and an ordered list of processing steps, the operators.
 */
@Value
class PipelineDto {
    String name;
    List<Step> steps;

    @Value
    @RequiredArgsConstructor
    public static class Step {
        String operator;
        String version;
        List<String> arguments;

        public Step(String operator, String version) {
            this.operator = operator;
            this.version = version;
            arguments = Collections.emptyList();
        }
    }
}
