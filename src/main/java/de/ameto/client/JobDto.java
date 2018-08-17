package de.ameto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@RequiredArgsConstructor
class JobDto {
    private String id;
    private final String asset;
    private final String pipeline;
    private final int status;
    private final String result;
}
