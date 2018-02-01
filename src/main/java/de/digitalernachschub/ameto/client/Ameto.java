package de.digitalernachschub.ameto.client;

import de.digitalernachschub.ameto.client.dto.Job;
import de.digitalernachschub.ameto.client.dto.Pipeline;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Ameto {
    private final Retrofit retrofit;
    private final AmetoApi ameto;

    public Ameto(String url) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
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
            List<Pipeline.Step> steps_ = steps.stream().map(Pipeline.Step::new).collect(Collectors.toList());
            Pipeline pipeline = new Pipeline(name, steps_);
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
     * Returns a list of all pipelines.
     * @return List of pipelines
     */
    public List<String> getPipelines() {
        List<Pipeline> pipelines = Collections.emptyList();
        try {
            Response<List<Pipeline>> response = ameto.getPipelines().execute();
            pipelines = response.body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pipelines.stream().map(Pipeline::getName).collect(Collectors.toList());
    }

    public Asset add(Path assetPath) {
        try {
            byte[] assetContent = Files.readAllBytes(assetPath);
            RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), assetContent);
            Response<AddAssetResponse> response = ameto.add(body).execute();
            return new Asset(response.body().getId());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to upload asset data to ameto.", e);
        }
    }

    public List<String> getJobs() {
        Response<List<Job>> response;
        try {
            response = ameto.getJobs().execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        val jobs = response.body();
        return jobs.stream().map(Job::getId).collect(Collectors.toList());
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
