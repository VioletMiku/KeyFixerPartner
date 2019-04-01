package com.keyfixer.partner;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCallActivity extends AppCompatActivity implements View.OnClickListener {

    TextView txttime, txtdistance, txtaddress;
    MediaPlayer mediaPlayer;
    IGoogleAPI mservice;
    IFCMService mFCMervice;
    Button btnAccept, btnDecline;
    String strCustomerId;
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
        btnDecline = (Button) findViewById(R.id.btn_decline);
        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);

        mservice = Common.getGoogleAPI();
        mFCMervice = Common.getFCMService();

        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent() != null){
            lat = getIntent().getDoubleExtra("lat",-1.0);
            lng = getIntent().getDoubleExtra("lng",-1.0);
            strCustomerId = getIntent().getStringExtra("customer");
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
                        JSONArray routes =  jsonObject.getJSONArray("routes");
                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");
                        JSONObject legsObject = legs.getJSONObject(0);
                        JSONObject distance = legsObject.getJSONObject("distance");
                        JSONObject time = legsObject.getJSONObject("duration");
                        String address = legsObject.getString("end_address");

                        txttime.setText(time.getString("text"));
                        txtdistance.setText(distance.getString("text"));
                        txtaddress.setText(address);
                        Log.d("data","time: " + txttime.getText() + ", distance: " + txtdistance.getText() + ", address: " + txtaddress.getText());

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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_accept:
                Intent intent = new Intent(CustomerCallActivity.this, FixerTracking.class);
                //send customer location to new activity
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("customerId",strCustomerId);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_decline:
                if (!TextUtils.isEmpty(strCustomerId))
                    cancelBooking(strCustomerId);
                break;
        }
    }

    private void cancelBooking(String strCustomerId) {
        Token token = new Token(strCustomerId);
        Notification notification = new Notification("Thông báo hủy từ thợ sửa khóa!","Xin lỗi, mình có việc bận");
        Sender sender = new Sender(notification, token.getToken());
        mFCMervice.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
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