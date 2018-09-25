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

/**
 * Represents a meaningful piece of data.
 * Assets are archived by Ameto and will never be modified. Any operation to an @{code Asset} will produce a
 * {@link ProcessedAsset}. Such operations are available through {@link Pipeline}s.
 */
@RequiredArgsConstructor
@EqualsAndHashCode(of={"id"})
@Getter
public class Asset {
    private final String id;
    @Getter(AccessLevel.NONE)
    private final AmetoApi api;
    private Set<ProcessedAsset> variants;

    /**
     * Returns assets that were derived from this asset using a pipeline.
     * This information is retrieved lazily. If you are calling this method for the first time, a request to the
     * Ameto API will be sent to retrieve the available variants.
     * @return Processed assets
     * @throws AmetoException if the information could not be fetched
     */
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
