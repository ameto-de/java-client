package de.digitalernachschub.ameto.client;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AmetoClient {
    private final AmetoApi ameto;

    public AmetoClient(String url) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(httpClient)
                .build();
        ameto = retrofit.create(AmetoApi.class);
    }

    /**
     * Adds the specified pipeline to ameto.
     * If a pipeline with the specified name already exists, it will be overwritten.
     * @param pipeline Pipeline to be added
     */
    public void add(Pipeline pipeline) {
        Response<Void> response;
        try {
             response = ameto.add(pipeline).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a list of all pipelines.
     * @return List of pipelines
     */
    public List<Pipeline> getPipelines() {
        List<Pipeline> pipelines = Collections.emptyList();
        try {
            Response<List<Pipeline>> response = ameto.getPipelines().execute();
            pipelines = response.body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pipelines;
    }

    public String add(Path assetPath) {
        try {
            byte[] assetContent = Files.readAllBytes(assetPath);
            Response<AddAssetResponse> response = ameto.add(assetContent).execute();
            return response.body().getId();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to upload asset data to ameto.", e);
        }
    }

    public String add(Job job) {
        Response<String> response = null;
        try {
            response = ameto.add(job).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.body();
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
