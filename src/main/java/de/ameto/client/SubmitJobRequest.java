package de.ameto.client;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;

@Value
class SubmitJobRequest {
    @JsonSerialize(converter = AssetReferenceSerializer.class)
    AssetReference asset;
    String pipeline;
}
