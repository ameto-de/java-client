package de.digitalernachschub.ameto.client;

import lombok.val;
import okhttp3.*;
import retrofit2.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Ameto {
    private final Retrofit retrofit;
    private final AmetoApi ameto;

    public Ameto(String url, String apiToken) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request alteredRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer "+apiToken)
                            .build();
                    return chain.proceed(alteredRequest);
                })
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(httpClient)
                .build();
        ameto = retrofit.create(AmetoApi.class);
    }

    /**
     * Adds the specified pipeline to ameto.
     * If a pipeline with the specified name already exists, it will be overwritten.
     * @param name Pipeline name
     * @param steps Processing steps
     */
    public de.digitalernachschub.ameto.client.Pipeline add(String name, List<String> steps) {
        Response<Void> response;
        try {
            List<PipelineDto.Step> steps_ = steps.stream().map(PipelineDto.Step::new).collect(Collectors.toList());
            PipelineDto pipeline = new PipelineDto(name, steps_);
            response = ameto.add(pipeline).execute();
            if (!response.isSuccessful()) {
                Converter<ResponseBody, AddPipelineError> errorConverter =
                        retrofit.responseBodyConverter(AddPipelineError.class, new Annotation[0]);
                AddPipelineError error = errorConverter.convert(response.errorBody());
                throw new RuntimeException(error.getError());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new de.digitalernachschub.ameto.client.Pipeline(ameto, name);
    }

    /**
     * Returns a collection of all pipelines.
     * @return Set of pipelines
     */
    public Set<Pipeline> getPipelines() {
        List<PipelineDto> pipelines;
        try {
            Response<List<PipelineDto>> response = ameto.getPipelines().execute();
            if (!response.isSuccessful()) {
                Converter<ResponseBody, AddPipelineError> errorConverter =
                        retrofit.responseBodyConverter(AddPipelineError.class, new Annotation[0]);
                AddPipelineError error = errorConverter.convert(response.errorBody());
                throw new RuntimeException(error.getError());
            }
            pipelines = response.body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pipelines.stream()
                .map(pipelineDto -> new Pipeline(ameto, pipelineDto.getName()))
                .collect(Collectors.toSet());
    }

    /**
     * Returns Assets that have been uploaded to Ameto.
     * Note that the returned collection may not include very recently uploaded items.
     * @return Assets available to Ameto
     * @throws AmetoException if the asset list could not be retrieved
     */
    public Set<Asset> getAssets() {
        Response<List<String>> assetIds;
        try {
            assetIds = ameto.getAssets().execute();
        } catch (IOException e) {
            throw new AmetoException("Unable to retrieve assets from Ameto.", e);
        }
        if (!assetIds.isSuccessful() || assetIds.body() == null) {
            throw new AmetoException("Unsuccessful response from Ameto. This is either a bug in Ameto or you " +
                    "are using this client with an incompatible version of Ameto (e.g. wrong API version).");
        }
        return assetIds.body().stream()
                .map(Asset::new)
                .collect(Collectors.toSet());
    }

    /**
     * Uploads the asset under the specified path.
     * @param assetPath Path to asset file
     * @throws AmetoException if an error occurs during asset upload
     */
    public Asset add(Path assetPath) {
        try {
            byte[] assetContent = Files.readAllBytes(assetPath);
            RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), assetContent);
            Response<AddAssetResponse> response = ameto.add(body).execute();
            if (!response.isSuccessful() || response.body() == null) {
                throw new AmetoException("Received error response from Ameto API");
            }
            return new Asset(response.body().getId());
        } catch (IOException e) {
            throw new AmetoException("Unable to upload asset data to ameto.", e);
        }
    }

    public List<de.digitalernachschub.ameto.client.Job> getJobs() {
        Response<List<JobDto>> response;
        try {
            response = ameto.getJobs().execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        val jobs = response.body();
        return jobs.stream()
                .map(job -> new de.digitalernachschub.ameto.client.Job(
                        job.getId(), job.getAsset(), job.getPipeline(), jobStatus(job.getStatus())))
                .collect(Collectors.toList());
    }

    public Job.Status jobStatus(int status) {
        switch (status) {
            case 0:
                return Job.Status.Pending;
            case 1:
                return Job.Status.InProgress;
            case 2:
                return Job.Status.Finished;
            case 3:
                return Job.Status.Failed;
            default:
                throw new IllegalArgumentException("Unknown job status: " + status);
        }
    }

    public List<Operator> getOperators() {
        try {
            Response<List<Operator>> response = ameto.getOperators().execute();
            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
