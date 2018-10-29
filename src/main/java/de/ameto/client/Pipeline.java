package de.ameto.client;

import de.ameto.client.operators.Operator;
import de.ameto.client.operators.Resize;
import de.ameto.client.operators.Shrink;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public enum Format {
        Jpeg
    }

    public static class Builder {
        private final AmetoApi api;
        private final String name;
        private final List<Operator> steps;

        Builder(AmetoApi api, String name) {
            this.api = api;
            this.name = name;
            steps = new ArrayList<>();
        }

        public Builder resize(int width, int height) {
            return this.resize(width, height, Resize.Mode.FIT);
        }

        public Builder resize(int width, int height, Resize.Mode mode) {
            steps.add(new Resize(width, height, mode));
            return this;
        }

        public Builder format(Format format) {
            Operator outputOperator;
            if (format == Format.Jpeg) {
                outputOperator = new Shrink();
            } else {
                throw new IllegalArgumentException("Unknown output format: " + format);
            }
            steps.add(outputOperator);
            return this;
        }

        public Pipeline build() {
            Response<PipelineDto> response;
            try {
                List<PipelineDto.Step> steps_ = steps.stream()
                        .map(op -> new PipelineDto.Step(op.getName(), op.getVersion(), op.getArguments()))
                        .collect(Collectors.toList());
                PipelineDto pipeline = new PipelineDto(name, steps_);
                response = api.add(pipeline).execute();
                if (!response.isSuccessful()) {
                    Optional<ResponseBody> errorResponse = Optional.ofNullable(response.errorBody());
                    if (errorResponse.isPresent()) {
                        throw new AmetoException(errorResponse.get().string());
                    }
                    throw new AmetoException("An error occurred when submitting the pipeline to the server.");
                }
            } catch (IOException e) {
                throw new AmetoException("Unable to send pipeline request to the Ameto API server", e);
            }
            String pipelineId = response.body().getId();
            return new Pipeline(api, pipelineId, name, steps);
        }
    }

    /**
     * Applies this processing pipeline to the specified asset.
     * This method triggers a job and polls Ameto until the result is available.
     * @param asset Asset to be processed
     * @return Processed asset
     * @throws AmetoException if the pipeline job could not be submitted
     * @throws AmetoException if the asset could not be processed
     */
    public ProcessedAsset push(Asset asset) {
        String jobId = submitJob(new AssetReference(asset.getId()), getId());
        try {
            int retries = 4;
            long retryBackoff = 5000L;
            for (int attempt = 0; attempt < retries; attempt++) {
                Response<JobDto> jobsResponse = api.getJob(jobId).execute();
                Optional<JobDto> currentJob = Optional.ofNullable(jobsResponse.body());
                if (currentJob.isPresent() && currentJob.get().getStatus() == Job.Status.Finished) {
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
            return Optional.ofNullable(getJobResponse.body())
                    .map(j -> new ProcessedAsset(j.getResult().getId(), api))
                    .orElseThrow(() -> new AmetoException("Fetching job information was successful, but the response is empty"));
        } catch (IOException e) {
            throw new AmetoException("Failed to process asset in pipeline", e);
        }
    }

    private String submitJob(AssetReference asset, String pipeline) {
        SubmitJobRequest job = new SubmitJobRequest(asset, pipeline);
        Response<SubmitJobResponse> addJobResponse = null;
        try {
            addJobResponse = api.add(job).execute();
        } catch (IOException e) {
            throw new AmetoException("Unable to submit job.");
        }
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
        Optional<SubmitJobResponse> jobResponse = Optional.ofNullable(addJobResponse.body());
        return jobResponse
                .orElseThrow(() -> new AmetoException("Job submission returned empty response"))
                .getId();
    }
}
