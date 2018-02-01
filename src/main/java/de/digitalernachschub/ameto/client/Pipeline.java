package de.digitalernachschub.ameto.client;

import de.digitalernachschub.ameto.client.dto.Job;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import retrofit2.Response;

import java.io.IOException;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Pipeline {
    private final AmetoApi api;
    @Getter
    private final String name;

    public String push(Asset asset) {
        Job job = new Job(asset.getId(), getName());
        try {
            Response<String> addAssetResponse = api.add(job).execute();
            String assetUrl = addAssetResponse.body();
            return assetUrl;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
