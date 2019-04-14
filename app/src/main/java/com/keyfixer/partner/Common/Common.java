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
    public static final int PICK_IMAGE_REQUEST = 9999;
    public static final String user_field = "user";
    public static final String pwd_field = "password";
    private static double house_lock_service = 30000;
    private static double car_lock_service = 25000;
    private static double motorbyke_lock_service = 20000;
    private static double distance_rate = 1.75;

    public static User currentUser;
    public static Location mLastLocation = null;

    public static final String baseUrl = "https://maps.googleapis.com";
    public static final String fcmUrl = "https://fcm.googleapis.com/";

    public static double formulaPrice(double km, double[] service_fee){
        int quantity_of_service = service_fee.length;
        double fee = 0;
        for (int index = 0 ; index < quantity_of_service ; index ++)
            fee += service_fee[index];
        if (km < 2){
            return fee;
        } else {
            return fee + (km*4000);
        }
    }

    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseUrl).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmUrl).create(IFCMService.class);
    }
}
