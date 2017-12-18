package de.digitalernachschub.ameto.client;

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
    Call<AddAssetResponse> add(@Body byte[] asset);
}
