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

    public Future<ProcessedAsset> push(Asset asset) {
        JobDto job = new JobDto(asset.getId(), getName());
        try {
            Response<String> addAssetResponse = api.add(job).execute();
            String assetUrl = addAssetResponse.body();
            OkHttpClient http = new OkHttpClient();
            Request getProcessedAsset = new Request.Builder()
                    .url(assetUrl)
                    .build();
            CompletableFuture<ProcessedAsset> result = new CompletableFuture<>();
            Callback processAssetCallback = new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    result.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    String assetId = new URL(assetUrl).getPath();
                    ProcessedAsset processedAsset = new ProcessedAsset(assetId, response.body().bytes());
                    result.complete(processedAsset);
                }
            };
            http.newCall(getProcessedAsset).enqueue(processAssetCallback);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
