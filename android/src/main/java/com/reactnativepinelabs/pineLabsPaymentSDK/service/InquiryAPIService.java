package com.kotak.bnpl.pineLabsPaymentSDK.service;


import com.kotak.bnpl.pineLabsPaymentSDK.model.InquiryResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface InquiryAPIService {
    @GET("/api/v1/inquiry/order/{order}/payment/{payment}")
    Call<InquiryResponse> enquiryAPI(@Path("order") String order, @Path("payment") String payment);
}
