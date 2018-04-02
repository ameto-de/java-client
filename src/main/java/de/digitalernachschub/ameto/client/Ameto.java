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
            e.printStackTrace();
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
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return pipelines.stream()
                .map(pipelineDto -> new Pipeline(ameto, pipelineDto.getName()))
                .collect(Collectors.toSet());
    }

    public Asset add(Path assetPath) {
        try {
            byte[] assetContent = Files.readAllBytes(assetPath);
            RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), assetContent);
            Response<AddAssetResponse> response = ameto.add(body).execute();
            val asset = new Asset(response.body().getId());
            return asset;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to upload asset data to ameto.", e);
        }
    }

    public List<de.digitalernachschub.ameto.client.Job> getJobs() {
        Response<List<JobDto>> response;
        try {
            response = ameto.getJobs().execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        val jobs = response.body();
        return jobs.stream()
                .map(job -> new de.digitalernachschub.ameto.client.Job(
                        job.getId(), job.getAsset(), job.getPipeline()))
                .collect(Collectors.toList());
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
