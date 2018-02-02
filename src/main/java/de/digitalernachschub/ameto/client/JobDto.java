package de.digitalernachschub.ameto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@RequiredArgsConstructor
public class JobDto {
    private String id;
    private final String asset;
    private final String pipeline;
}
