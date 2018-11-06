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

    /**
     * Describes the output format of a Pipeline
     */
    public enum Format {
        /** Specifies the format {@code image/jpeg} */
        Jpeg
    }

    /**
     * Represents a class used to configure a processing Pipeline.
     */
    public static class Builder {
        private final AmetoApi api;
        private final String name;
        private final List<Operator> steps;

        Builder(AmetoApi api, String name) {
            this.api = api;
            this.name = name;
            steps = new ArrayList<>();
        }

        /**
         * Resizes the input asset to the specified dimensions.
         * The resize operation preserves aspect ratio and the output dimensions
         * never exceed the specified width and height.
         * <p>
         * This is identical to calling {@link #resize(int, int, Resize.Mode)} with {@code Resize.Mode.FIT}
         * as mode of operation.
         *
         * @param width  Target width
         * @param height Target height
         * @return Pipeline builder
         */
        public Builder resize(int width, int height) {
            return this.resize(width, height, Resize.Mode.FIT);
        }

        /**
         * Resizes the input asset to the specified dimensions using the specified mode of operation.
         * The mode specifies whether or not aspect ratio should be preserved and whether the specified dimensions
         * should be treated as an exact target, as a minimum size, or as a maximum size.
         * Please see {@link Resize.Mode} for the different resize behaviours.
         *
         * @param width  Target width
         * @param height Target height
         * @param mode   Resize mode
         * @return Pipeline builder
         */
        public Builder resize(int width, int height, Resize.Mode mode) {
            steps.add(new Resize(width, height, mode));
            return this;
        }

        /**
         * Specifies the output format for assets processed by this Pipeline.
         * As of now, the only output format is {@link Format#Jpeg}.
         *
         * @param format Output format
         * @return Pipeline builder
         */
        public FinalizableBuilder format(Format format) {
            Operator outputOperator;
            if (format == Format.Jpeg) {
                outputOperator = new Shrink();
            } else {
                throw new IllegalArgumentException("Unknown output format: " + format);
            }
            steps.add(outputOperator);
            return new FinalizableBuilder(api, name, steps);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static class FinalizableBuilder {
        private final AmetoApi api;
        private final String name;
        private final List<Operator> steps;

        /**
         * Creates a Pipeline from the Pipeline builder.
         * This method submits Pipeline information to Ameto and overwrites existing pipelines with the same name.
         * @return Pipeline object
         * @throws AmetoException if the Pipeline information could not be submitted
         */
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
        int retries = 6;
        double retryBackoff = 1000;
        double retryBackoffExponent = 1.1;
        for (int attempt = 0; attempt < retries; attempt++) {
            Response<GetJobResponse> jobsResponse;
            try {
                jobsResponse = api.getJob(jobId).execute();
            } catch (IOException e) {
                throw new AmetoException("Unable to request job data.", e);
            }
            if (!jobsResponse.isSuccessful() && jobsResponse.code() != 404) {
                Optional<ResponseBody> errorBody = Optional.ofNullable(jobsResponse.errorBody());
                String errorMessage = "An error occurred when fetching job information for job " + jobId;
                if (errorBody.isPresent()) {
                    try {
                        errorMessage = errorBody.get().string();
                    } catch (IOException e) {
                        throw new AmetoException("Unable to deserialize error message.", e);
                    }
                }
                throw new AmetoException(errorMessage);
            }
            Optional<ProcessedAsset> jobResult = Optional.ofNullable(jobsResponse.body())
                    .filter(j -> j.getStatus() == Job.Status.Finished)
                    .map(j -> new ProcessedAsset(j.getResult().get().getId(), api));
            if (jobResult.isPresent()) {
                return jobResult.get();
            }
            try {
                Thread.sleep((long) retryBackoff);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retryBackoff = Math.pow(retryBackoff, retryBackoffExponent);
        }
        throw new AmetoException("Job result could not be retrieved.");
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
