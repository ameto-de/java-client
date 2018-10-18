package de.ameto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

/**
 * Represents a processing pipeline.
 * A pipeline consists of a unique name and an ordered list of processing steps, the operators.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@RequiredArgsConstructor
class PipelineDto {
    private String id;
    private final String name;
    private final List<Step> steps;

    @Value
    public static class Step {
        String operator;
        String version;
        List<String> arguments;
    }
}
