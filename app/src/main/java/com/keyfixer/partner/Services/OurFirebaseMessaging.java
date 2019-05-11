package com.keyfixer.partner.Services;

import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.CustomerCallActivity;

import java.util.HashMap;
import java.util.Map;

public class OurFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Because of sending the firebase message with contain lat and lng from customer app
        //so we're going to convert message to Latng
        if (remoteMessage.getData() != null) {
            Map<String, String> data = remoteMessage.getData();
            String customer = data.get("customer");
            String lat = data.get("lat");
            String lng = data.get("lng");
            Common.service_chose = data.get("service");

            Intent intent = new Intent(getBaseContext() , CustomerCallActivity.class);
            intent.putExtra("lat" , lat);
            intent.putExtra("lng" , lng);
            intent.putExtra("customer" , customer);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
