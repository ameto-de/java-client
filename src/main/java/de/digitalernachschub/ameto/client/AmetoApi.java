package de.digitalernachschub.ameto.client;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

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
    @GET("/job/{jobId}")
    Call<JobDto> getJob(@Path("jobId") String jobId);
    @POST("/job")
    Call<String> add(@Body JobDto job);
    @GET("/operator")
    Call<List<Operator>> getOperators();
}
