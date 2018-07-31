package de.ameto.client;

import de.ameto.client.operators.Operator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Pipeline {
    private final AmetoApi api;
    @Getter
    private final String name;
    @Getter
    private final List<Operator> steps;

    public ProcessedAsset push(Asset asset) {
        int pendingJobStatus = 0;
        JobDto job = new JobDto(asset.getId(), getName(), pendingJobStatus, null);
        try {
            String jobId = submitJob(job);
            int retries = 3;
            long retryBackoff = 5000L;
            for (int attempt = 0; attempt < retries; attempt++) {
                Response<JobDto> jobsResponse = api.getJob(jobId).execute();
                Optional<JobDto> currentJob = Optional.ofNullable(jobsResponse.body());
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
            Response<JobDto> getJobResponse = api.getJob(jobId).execute();
            if (!getJobResponse.isSuccessful()) {
                Optional<ResponseBody> errorBody = Optional.ofNullable(getJobResponse.errorBody());
                String errorMessage = "An error occurred when fetching job information for job " + jobId;
                if (errorBody.isPresent()) {
                    errorMessage = errorBody.get().string();
                }
                throw new AmetoException(errorMessage);
            }
            Response<ResponseBody> jobResult = api.getResult(jobId).execute();
            if (!jobResult.isSuccessful()) {
                throw new AmetoException("Your job result could not be retrieved. " +
                        "It is possible that Ameto is experiencing a lot of traffic. Please try again later.");
            }
            Optional<ResponseBody> processedAssetResponseBody = Optional.ofNullable(jobResult.body());
            if (!processedAssetResponseBody.isPresent()) {
                throw new AmetoException("Received empty response for processed asset " + jobId);
            }
            return new ProcessedAsset(jobId, jobResult.body().byteStream());
        } catch (IOException e) {
            throw new AmetoException("Failed to process asset in pipeline", e);
        }
    }

    private String submitJob(JobDto job) throws IOException {
        Response<JobDto> addJobResponse = api.add(job).execute();
        Optional<JobDto> jobResponse = Optional.ofNullable(addJobResponse.body());
        return jobResponse
                .orElseThrow(() -> new AmetoException("Job submission returned empty response"))
                .getId();
    }
}
