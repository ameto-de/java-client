package de.digitalernachschub.ameto.client;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AmetoClient {
    private final AmetoApi ameto;

    public AmetoClient(String url) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
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
        try {
            Response<Void> response = ameto.add(pipeline).execute();
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
}
