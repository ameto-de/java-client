package de.digitalernachschub.ameto.client;

import de.digitalernachschub.ameto.client.dto.JobDto;
import de.digitalernachschub.ameto.client.dto.PipelineDto;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import java.util.List;

interface AmetoApi {
    @POST("/pipeline")
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
