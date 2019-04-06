package com.keyfixer.partner.Common;

import android.location.Location;

import com.keyfixer.partner.Model.User;
import com.keyfixer.partner.Remote.FCMClient;
import com.keyfixer.partner.Remote.IFCMService;
import com.keyfixer.partner.Remote.IGoogleAPI;
import com.keyfixer.partner.Remote.RetrofitClient;

public class Common {

    public static final String fixer_tbl = "Fixers";
    public static final String fixer_inf_tbl = "Users";
    public static final String customer_tbl = "Customers";
    public static final String fix_request_tbl = "FixRequest";
    public static final String token_tbl = "Tokens";

    public static User currentUser;
    public static Location mLastLocation = null;

    public static final String baseUrl = "https://maps.googleapis.com";
    public static final String fcmUrl = "https://fcm.googleapis.com/";

    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseUrl).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmUrl).create(IFCMService.class);
    }
}
