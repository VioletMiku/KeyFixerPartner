package com.keyfixer.partner.Remote;

import com.keyfixer.partner.Model.FCMResponse;
import com.keyfixer.partner.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAWlZxhLo:APA91bEW-sNMjkVUNYQ2rEaR_g7XLvLEecc2s7pxrC22OcQmAWpcZq57XlOtaB93-HKBL-QAqdOeJZcaZaaborPtuMCTBMuw2rsUN8HUZZu9-hBGXJyG8KYtTwSLbDXCNvqqqbbHh3v1"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}