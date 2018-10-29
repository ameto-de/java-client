package de.ameto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
class GetJobResponse {
    String id;
    @JsonDeserialize(converter = AssetReferenceDeserializer.class)
    AssetReference asset;
    String pipeline;
    @JsonDeserialize(converter = JobStatusDeserializer.class)
    Job.Status status;
    @JsonDeserialize(converter = AssetReferenceDeserializer.class)
    AssetReference result;

    public Optional<AssetReference> getResult() {
        return Optional.ofNullable(result);
    }
}
