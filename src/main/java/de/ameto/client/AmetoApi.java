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
    @GET("/asset/{assetId}")
    Call<ResponseBody> getAssetEssence(@Path("assetId") String assetId);
    @Headers({"Accept: application/json"})
    @GET("/asset/{assetId}")
    Call<AssetMetadata> getAsset(@Path("assetId") String assetId);
    @Multipart
    @POST("/asset")
    Call<AddAssetResponse> add(@Part MultipartBody.Part asset);
    @DELETE("/asset/{assetId}")
    Call<Void> remove(@Path("assetId") String assetId);
    @GET("/pipeline")
    Call<List<PipelineDto>> getPipelines();
    @POST("/pipeline")
    Call<PipelineDto> add(@Body PipelineDto pipeline);
    @GET("/job")
    Call<List<JobDto>> getJobs();
    @GET("/job/{jobId}")
    Call<GetJobResponse> getJob(@Path("jobId") String jobId);
    @POST("/job")
    Call<SubmitJobResponse> add(@Body SubmitJobRequest job);
    @GET("/operator")
    Call<List<OperatorDto>> getOperators();
}
