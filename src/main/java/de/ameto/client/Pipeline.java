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

/**
 * Represents a series of processing steps used to convert or transform assets.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Pipeline {
    private final AmetoApi api;
    @Getter
    private final String id;
    @Getter
    private final String name;
    @Getter
    private final List<Operator> steps;

    /**
     * Applies this processing pipeline to the specified asset.
     * This method triggers a job and polls Ameto until the result is available.
     * @param asset Asset to be processed
     * @return Processed asset
     * @throws AmetoException if the asset could not be processed
     */
    public ProcessedAsset push(Asset asset) {
        int pendingJobStatus = 0;
        JobDto job = new JobDto(asset.getId(), getId(), pendingJobStatus, null);
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
            return new ProcessedAsset(getJobResponse.body().getResult(), api);
        } catch (IOException e) {
            throw new AmetoException("Failed to process asset in pipeline", e);
        }
    }

    private String submitJob(JobDto job) throws IOException {
        Response<JobDto> addJobResponse = api.add(job).execute();
        if (!addJobResponse.isSuccessful()) {
            Optional<ResponseBody> errorResponse = Optional.ofNullable(addJobResponse.errorBody());
            String errorMessage = errorResponse.map(responseBody -> {
                try {
                    return responseBody.string();
                } catch (IOException e) {
                    throw new AmetoException("Unable to deserialize error message", e);
                }
            }).orElse("Unspecified error");
            throw new AmetoException("Job submission failed with the following error:" + errorMessage);
        }
        Optional<JobDto> jobResponse = Optional.ofNullable(addJobResponse.body());
        return jobResponse
                .orElseThrow(() -> new AmetoException("Job submission returned empty response"))
                .getId();
    }
}
