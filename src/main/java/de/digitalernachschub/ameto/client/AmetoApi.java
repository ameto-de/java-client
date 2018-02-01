package de.digitalernachschub.ameto.client;

import de.digitalernachschub.ameto.client.dto.Pipeline;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import java.util.List;

interface AmetoApi {
    @POST("/pipeline")
    Call<Void> add(@Body Pipeline pipeline);
    @GET("/pipeline")
    Call<List<Pipeline>> getPipelines();
    @POST("/asset")
    Call<AddAssetResponse> add(@Body RequestBody asset);
    @GET("/job")
    Call<List<Job>> getJobs();
    @POST("/job")
    Call<String> add(@Body Job job);
    @GET("/operator")
    Call<List<Operator>> getOperators();
}
