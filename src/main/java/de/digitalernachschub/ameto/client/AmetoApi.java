package de.digitalernachschub.ameto.client;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

import java.util.List;

interface AmetoApi {
    @GET("/asset")
    Call<List<String>> getAssets();
    @POST("/asset")
    Call<AddAssetResponse> add(@Body RequestBody asset);
    @GET("/pipeline")
    Call<List<PipelineDto>> getPipelines();
    @PUT("/pipeline")
    Call<Void> add(@Body PipelineDto pipeline);
    @GET("/job")
    Call<List<JobDto>> getJobs();
    @POST("/job")
    Call<String> add(@Body JobDto job);
    @GET("/operator")
    Call<List<Operator>> getOperators();
}
