package de.digitalernachschub.ameto.client;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

import java.util.List;

interface AmetoApi {
    @PUT("/pipeline")
    Call<Void> add(@Body PipelineDto pipeline);
    @GET("/pipeline")
    Call<List<PipelineDto>> getPipelines();
    @POST("/asset")
    Call<AddAssetResponse> add(@Body RequestBody asset);
    @GET("/job")
    Call<List<JobDto>> getJobs();
    @POST("/job")
    Call<String> add(@Body JobDto job);
    @GET("/operator")
    Call<List<Operator>> getOperators();
}
