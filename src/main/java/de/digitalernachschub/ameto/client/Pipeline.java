package de.digitalernachschub.ameto.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.ResponseBody;
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
        JobDto job = new JobDto(asset.getId(), getName(), pendingJobStatus, null);
        try {
            int retries = 3;
            long retryBackoff = 5000L;
            for (int attempt = 0; attempt < retries; attempt++) {
                Response<List<JobDto>> jobsResponse = api.getJobs().execute();
                Optional<JobDto> currentJob = jobsResponse.body().stream()
                        .filter(j -> j.getAsset().equals(job.getAsset()))
                        .filter(j -> j.getPipeline().equals(job.getPipeline()))
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
            Response<String> addJobResponse = api.add(job).execute();
            String jobUrl = addJobResponse.body();
            String[] jobPath = new URL(jobUrl).getPath().split("/");
            String jobId = jobPath[jobPath.length - 1];
            Response<JobDto> getJobResponse = api.getJob(jobId).execute();
            if (!getJobResponse.isSuccessful()) {
                throw new AmetoException(getJobResponse.errorBody().string());
            }
            Response<ResponseBody> jobResult = api.getResult(jobId).execute();
            return new ProcessedAsset(jobId, jobResult.body().byteStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
