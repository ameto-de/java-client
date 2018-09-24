package de.ameto.client;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import retrofit2.Response;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@EqualsAndHashCode(of={"id"})
@Getter
public class Asset {
    private final String id;
    @Getter(AccessLevel.NONE)
    private final AmetoApi api;
    private Set<ProcessedAsset> variants;

    public Set<ProcessedAsset> getVariants() {
        if (variants == null) {
            Response<AssetMetadata> metadata;
            try {
                metadata = api.getAsset(this.id).execute();
            } catch (IOException e) {
                throw new AmetoException("Unable to retrieve metadata for asset " + this.id, e);
            }
            variants = Optional.ofNullable(metadata.body())
                    .orElseThrow(() -> new AmetoException("Received empty response when fetching metadata for asset " + this.id))
                    .getVariants().stream()
                    .map(processedAssetId -> new ProcessedAsset(processedAssetId, api))
                    .collect(Collectors.toSet());
        }
        return variants;
    }
}
