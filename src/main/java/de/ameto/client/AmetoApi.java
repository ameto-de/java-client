package de.ameto.client;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

interface AmetoApi {
    @Headers({"Accept: application/json"})
    @GET("/asset")
    Call<List<String>> getAssets();
    @Multipart
    @POST("/asset")
    Call<AddAssetResponse> add(@Part MultipartBody.Part asset);
    @DELETE("/asset/{assetId}")
    Call<Void> remove(@Path("assetId") String assetId);
    @GET("/pipeline")
    Call<List<PipelineDto>> getPipelines();
    @POST("/pipeline")
    Call<Void> add(@Body PipelineDto pipeline);
    @GET("/job")
    Call<List<JobDto>> getJobs();
    @GET("/job/{jobId}")
    Call<JobDto> getJob(@Path("jobId") String jobId);
    @POST("/job")
    Call<JobDto> add(@Body JobDto job);
    @GET("/operator")
    Call<List<OperatorDto>> getOperators();
    @GET("/processed/{jobId}")
    Call<ResponseBody> getResult(@Path("jobId") String jobId);
}
