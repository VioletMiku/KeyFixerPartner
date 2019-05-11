package com.keyfixer.partner.Services;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

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

            String customerRequest = data.get("customer");
            try{
                if (customerRequest.equals("Cancel")){
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OurFirebaseMessaging.this, "Khách hàng đã hủy", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else{

                    final String message = data.get("message");
                    String lat = data.get("lat");
                    String lng = data.get("lng");
                    Common.service_chose = data.get("service");

                    Intent intent = new Intent(getBaseContext() , CustomerCallActivity.class);
                    intent.putExtra("lat" , lat);
                    intent.putExtra("lng" , lng);
                    intent.putExtra("customer" , customerRequest);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }catch (Exception ex){
                Log.e("Warning","Something wrong");
                Log.e("Exception", "" + ex.getCause());
            }
        }
    }
}
