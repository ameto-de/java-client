package de.digitalernachschub.ameto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
class AddAssetResponse {
    String id;
}
