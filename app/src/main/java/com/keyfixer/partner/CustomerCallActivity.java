package com.keyfixer.partner;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Model.FCMResponse;
import com.keyfixer.partner.Model.Notification;
import com.keyfixer.partner.Model.Sender;
import com.keyfixer.partner.Model.Token;
import com.keyfixer.partner.Remote.IFCMService;
import com.keyfixer.partner.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCallActivity extends AppCompatActivity implements View.OnClickListener {

    TextView txttime, txtdistance, txtaddress;
    Button btnAccept, btnDecline;
    MediaPlayer mediaPlayer;
    IGoogleAPI mservice;
    String customerId = "";
    IFCMService ifcmservice;
    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);
        Initializing();
    }

    private void Initializing() {
        txttime = (TextView) findViewById(R.id.txt_time);
        txtdistance = (TextView) findViewById(R.id.txt_distance);
        txtaddress = (TextView) findViewById(R.id.txt_Address);

        btnAccept = (Button) findViewById(R.id.btn_accept);
        btnAccept.setOnClickListener(this);
        btnDecline = (Button) findViewById(R.id.btn_decline);
        btnDecline.setOnClickListener(this);

        mservice = Common.getGoogleAPI();
        ifcmservice = Common.getFCMService();

        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent() != null){
            lat = getIntent().getDoubleExtra("lat",-1.0);
            lng = getIntent().getDoubleExtra("lng",-1.0);
            customerId = getIntent().getStringExtra("customer");
            getDirection(lat, lng);
        }
    }

    private void getDirection(double lat, double lng) {
        String requestAPI = null;

        try{
            requestAPI = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + Common.mLastLocation.getLatitude() + "," + Common.mLastLocation.getLongitude() + "&" +
                    "destination=" + lat + "," + lng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            Log.d("MikuRoot", requestAPI); //print url to debug

            mservice.getPath(requestAPI).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try{
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");
                        JSONObject legsObject = legs.getJSONObject(0);
                        //get distance
                        JSONObject distance = legsObject.getJSONObject("distance");
                        txtdistance.setText(distance.getString("text"));
                        Log.d("distance", "" + distance.getString("text"));
                        //get time
                        JSONObject time = legsObject.getJSONObject("duration");
                        txttime.setText(time.getString("text"));
                        Log.d("time", "" + time.getString("text"));
                        //get address
                        String address = legsObject.getString("end_address");
                        txtaddress.setText(address);
                        Log.d("address", "" + address);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(CustomerCallActivity.this, "Không thể lấy được đường đi, xin vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    System.out.print(t.getMessage());
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mediaPlayer.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_accept:
                Intent intent = new Intent(CustomerCallActivity.this, FixerTracking.class);
                //send customer location to new activity
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("customerId",customerId);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_decline:
                if (!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
                break;
        }
    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);
        Notification notification = new Notification("Thông báo hủy từ thợ sửa khóa!","Xin lỗi, mình có việc bận");
        Sender sender = new Sender(token.getToken(), notification);
        ifcmservice.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success == 1){
                    Toast.makeText(CustomerCallActivity.this, "Đã hủy!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Toast.makeText(CustomerCallActivity.this, "The fucking capitalism took the internet... Connection error, cyka blyat!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
