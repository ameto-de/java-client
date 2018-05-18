package de.digitalernachschub.ameto.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Pipeline {
    private final AmetoApi api;
    @Getter
    private final String name;

    public ProcessedAsset push(Asset asset) {
        int pendingJobStatus = 0;
        JobDto job = new JobDto(asset.getId(), getName(), pendingJobStatus);
        try {
            Response<String> addAssetResponse = api.add(job).execute();
            String assetUrl = addAssetResponse.body();
            Request getProcessedAsset = new Request.Builder()
                    .url(assetUrl)
                    .build();
            OkHttpClient http = new OkHttpClient();
            int retries = 3;
            long retryBackoff = 5000L;
            for (int attempt = 0; attempt < retries; attempt++) {
                Response<List<JobDto>> jobsResponse = api.getJobs().execute();
                Optional<JobDto> currentJob = jobsResponse.body().stream()
                        .filter(j -> j.getId().equals(job.getId()))
                        .findAny();
                int finishedJobStatus = 2;
                if (currentJob.isPresent() && currentJob.get().getStatus() == finishedJobStatus) {
                    break;
                }
                try {
                    Thread.sleep(retryBackoff);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            okhttp3.Response fetchResultResponse = http.newCall(getProcessedAsset).execute();
            if (!fetchResultResponse.isSuccessful()) {
                String message = fetchResultResponse.message();
                fetchResultResponse.close();
                throw new AmetoException(message);
            }
            String[] assetPath = new URL(assetUrl).getPath().split("/");
            String assetId = assetPath[assetPath.length - 1];
            ProcessedAsset processedAsset = new ProcessedAsset(assetId, fetchResultResponse.body().bytes());
            fetchResultResponse.close();
            return processedAsset;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
