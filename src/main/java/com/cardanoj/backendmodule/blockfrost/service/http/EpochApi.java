package com.cardanoj.backendmodule.blockfrost.service.http;

import com.cardanoj.coreapi.model.ProtocolParams;
import com.cardanoj.backend.model.EpochContent;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface EpochApi {

    @GET("epochs/latest")
    Call<EpochContent> getLatestEpoch(@Header("project_id")  String projectId);

    @GET("epochs/{number}")
    Call<EpochContent> getEpochByNumber(@Header("project_id")  String projectId, @Path("number") Integer number);

    @GET("epochs/{number}/parameters")
    Call<ProtocolParams> getProtocolParameters(@Header("project_id")  String projectId, @Path("number") Integer number);

}
