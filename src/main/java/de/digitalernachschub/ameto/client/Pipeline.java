package de.digitalernachschub.ameto.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Pipeline {
    private final AmetoApi api;
    @Getter
    private final String name;

    public ProcessedAsset push(Asset asset) {
        JobDto job = new JobDto(asset.getId(), getName());
        try {
            Response<String> addAssetResponse = api.add(job).execute();
            String assetUrl = addAssetResponse.body();
            Request getProcessedAsset = new Request.Builder()
                    .url(assetUrl)
                    .build();
            OkHttpClient http = new OkHttpClient();
            int retries = 3;
            long retryBackoff = 3000L;
            okhttp3.Response response = null;
            for (int attempt = 0; attempt < retries; attempt++) {
                response = http.newCall(getProcessedAsset).execute();
                if (response.isSuccessful()) {
                    break;
                }
                try {
                    Thread.sleep(retryBackoff);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!response.isSuccessful()) {
                throw new AmetoException(response.message());
            }
            String[] assetPath = new URL(assetUrl).getPath().split("/");
            String assetId = assetPath[assetPath.length - 1];
            return new ProcessedAsset(assetId, response.body().bytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
