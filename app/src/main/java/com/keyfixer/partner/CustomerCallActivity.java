package com.keyfixer.partner;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCallActivity extends AppCompatActivity {

    TextView txttime, txtdistance, txtaddress;
    MediaPlayer mediaPlayer;
    IGoogleAPI mservice;

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

        mservice = Common.getGoogleAPI();

        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent() != null){
            double lat = getIntent().getDoubleExtra("lat",-1.0);
            double lng = getIntent().getDoubleExtra("lng",-1.0);
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
}
