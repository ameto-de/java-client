package de.ameto.client;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Represents the result of applying a {@link Pipeline} to an {@code Asset}.
 */
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Getter
public class ProcessedAsset {
    private final String id;
    @Getter(AccessLevel.NONE)
    private final AmetoApi api;
    private InputStream essence;

    /**
     * Returns the actual binary data of this processed asset.
     * This information is retrieved lazily. If you are calling this method for the first time, a request to the
     * Ameto API will be sent to retrieve the asset essence.
     * @return Asset essence
     * @throws AmetoException if the information could not be fetched
     */
    public InputStream getEssence() {
        if (essence == null) {
            Response<ResponseBody> getJobResult;
            try {
                getJobResult = api.getAssetEssence(this.id).execute();
            } catch (IOException e) {
                throw new AmetoException("Unable to fetch essence of asset " + this.id, e);
            }
            if (!getJobResult.isSuccessful()) {
                throw new AmetoException("Your job result could not be retrieved. " +
                        "It is possible that Ameto is experiencing a lot of traffic. Please try again later.");
            }
            Optional<ResponseBody> processedAssetResponseBody = Optional.ofNullable(getJobResult.body());
            essence = processedAssetResponseBody.map(ResponseBody::byteStream)
                    .orElseThrow(() -> new AmetoException("Received empty response for processed asset " + this.id));
        }
        return essence;
    }
}
