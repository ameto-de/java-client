package de.digitalernachschub.ameto.client;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AmetoClient {
    private final AmetoApi ameto;

    public AmetoClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:5000")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        ameto = retrofit.create(AmetoApi.class);
    }

    public void add(Pipeline pipeline) {
        try {
            Response<Void> response = ameto.add(pipeline).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
