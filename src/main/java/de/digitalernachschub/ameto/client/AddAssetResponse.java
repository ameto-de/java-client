package de.digitalernachschub.ameto.client;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.net.URI;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
class AddAssetResponse {
    @JsonProperty("asset_uri")
    URI assetUri;

    @JsonGetter("asset_uri")
    public URI getAssetUri() {
        return assetUri;
    }
}
