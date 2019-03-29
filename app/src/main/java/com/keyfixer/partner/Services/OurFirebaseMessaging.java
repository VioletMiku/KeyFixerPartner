package com.keyfixer.partner.Services;

import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.keyfixer.partner.CustomerCallActivity;

public class OurFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Because of sending the firebase message with contain lat and lng from customer app
        //so we're going to convert message to Latng
        LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);
        Intent intent = new Intent(getBaseContext(), CustomerCallActivity.class);
        intent.putExtra("lat", customer_location.latitude);
        intent.putExtra("lng", customer_location.longitude);
        startActivity(intent);
    }
}
