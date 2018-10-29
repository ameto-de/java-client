package de.ameto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@RequiredArgsConstructor
class JobDto {
    private String id;
    @JsonSerialize(converter = AssetReferenceSerializer.class)
    @JsonDeserialize(converter = AssetReferenceDeserializer.class)
    private final AssetReference asset;
    private final String pipeline;
    private final int status;
    @JsonSerialize(converter = AssetReferenceSerializer.class)
    @JsonDeserialize(converter = AssetReferenceDeserializer.class)
    private final AssetReference result;
}
