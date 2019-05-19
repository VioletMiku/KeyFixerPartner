package com.keyfixer.partner;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Helper.DirectionJSONParser;
import com.keyfixer.partner.Model.DataMessage;
import com.keyfixer.partner.Model.FCMResponse;
import com.keyfixer.partner.Model.Fixer;
import com.keyfixer.partner.Model.Service;
import com.keyfixer.partner.Model.Token;
import com.keyfixer.partner.Remote.IFCMService;
import com.keyfixer.partner.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FixerTracking extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener {

    private GoogleMap mMap;
    IGoogleAPI mService;
    IFCMService ifcmService;

    GeoFire geofire;
    String customerid;
    Button btnstartfixing, btnCancel;
    List<Service> services;

    String customerlat, customerlng;
    double fee;
    //play services
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleAPiClient;
    Location fixLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private Circle customerMarker;
    private Marker fixermarker;
    private Polyline direction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixer_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Initializing();
    }

    private void Initializing() {
        if (getIntent() != null){
            customerlat = getIntent().getStringExtra("lat");
            customerlng = getIntent().getStringExtra("lng");
            customerid = getIntent().getStringExtra("customerId");
        }
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        mService = Common.getGoogleAPI();
        ifcmService = Common.getFCMService();
        btnstartfixing = (Button) findViewById(R.id.btnstart);
        btnstartfixing.setOnClickListener(this);
        setupLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("used service", "" + Common.used_service);
        Log.e("used service 1", "" + Common.used_service1);
        Log.e("used service 2", "" + Common.used_service2);
        mMap = googleMap;
        customerMarker = mMap.addCircle(new CircleOptions().center(new LatLng(Double.parseDouble(customerlat), Double.parseDouble(customerlng)))
                .radius(50).strokeColor(Color.BLUE).fillColor(0x220000FF).strokeWidth(5.0f));
        if (!TextUtils.isEmpty(Common.used_service))
            geofire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).child("S ử a   k h ó a   n h à").child("activated"));
        if (!TextUtils.isEmpty(Common.used_service1))
            geofire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).child("S ử a   k h ó a   x e   h ơ i").child("activated"));
        if (!TextUtils.isEmpty(Common.used_service2))
            geofire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).child("S ử a   k h ó a   x e   g ắ n   m á y").child("activated"));
        GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(Double.parseDouble(customerlat), Double.parseDouble(customerlng)), 0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key , GeoLocation location) {
                btnstartfixing.setEnabled(true);
                sendArrivedNotification(customerid);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key , GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendArrivedNotification(String customerid) {

        Token token = new Token(customerid);

        Map<String, String> content = new HashMap<>();
        content.put("title","Đã đến");
        content.put("message","Thợ sửa bạn đặt đã đến, chúc bạn một ngày vui vẻ!");
        DataMessage dataMessage = new DataMessage(token.getToken(), content);
        ifcmService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call , Response<FCMResponse> response) {
                if (response.body().success != 1){
                    Toast.makeText(FixerTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call , Throwable t) {

            }
        });
    }

    private void sendFixCompleteNotification(String customerid) {

        Token token = new Token(customerid);

        Map<String, String> content = new HashMap<>();
        content.put("title","Sửa xong");
        content.put("message", customerid);
        DataMessage dataMessage = new DataMessage(token.getToken(), content);
        ifcmService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call , Response<FCMResponse> response) {
                if (response.body().success != 1){
                    Toast.makeText(FixerTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call , Throwable t) {

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPiClient, mLocationRequest, this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleAPiClient);
        if (Common.mLastLocation != null){//co bug cho nay _ mlastLocation = null
            final double latitude = Common.mLastLocation.getLatitude();
            final double longtitude = Common.mLastLocation.getLongitude();
            if (fixermarker != null)
                fixermarker.remove();
            fixermarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longtitude)).title("Bạn")
                    .icon(BitmapDescriptorFactory.defaultMarker()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtitude),17.0f));
            if (direction != null)
                direction.remove(); // remove old direction
            getDirection();
        }
        else{
            Log.d("Ối!", "Không thể xác định được vị trí của bạn");
        }
    }

    private void getDirection() {
        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
        String requestAPI = null;

        try{
            requestAPI = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + customerlat + "," + customerlng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            Log.d("MikuRoot", requestAPI); //print url to debug
            mService.getPath(requestAPI).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try{
                        new ParserTask().execute(response.body().toString());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(FixerTracking.this, "Không thể lấy được đường đi, xin vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    System.out.print(t.getMessage());
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void setupLocation() {
        if (checkPlaySerives()){
            buildGoogleApiClient();
            createLocationRequest();
            displayLocation();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleAPiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleAPiClient.connect();
    }

    private boolean checkPlaySerives() {
        int result_code = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (result_code != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(result_code))
                GooglePlayServicesUtil.getErrorDialog(result_code, this, PLAY_SERVICE_RES_REQUEST).show();
            else{
                Toast.makeText(this, "Thiết bị này chưa hỗ trợ google play services", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleAPiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnstart:
                if (btnstartfixing.getText().equals("B Ắ T   Đ Ầ U   S Ử A")){
                    fixLocation = Common.mLastLocation;
                    RemoveLocation();
                    btnstartfixing.setText("S Ử A   H O À N   T Ấ T");
                } else if (btnstartfixing.getText().equals("S Ử A   H O À N   T Ấ T")){
                    calculateCashFee(fixLocation, Common.mLastLocation);
                    RemoveFixRequest();
                }
                break;
            case R.id.btnCancel:
                if (!TextUtils.isEmpty(customerid))
                    cancelBooking(customerid);
                Common.DecreaseRate(Common.FixerID);
                Intent home = new Intent(FixerTracking.this, FixerHome.class);
                startActivity(home);
                break;
        }
    }

    private void RemoveFixRequest() {
        DatabaseReference fixRequest_tbl = FirebaseDatabase.getInstance().getReference(Common.fix_request_tbl);
        fixRequest_tbl.child(Common.FixerID).removeValue();
    }

    private void RemoveLocation(){
        try {
            FirebaseDatabase.getInstance().goOffline();//set disconnected when the fixer leave
            Common.fusedLocationProviderClient.removeLocationUpdates(Common.locationCallback);
            Common.mCurrent.remove();
            mMap.clear();
        } catch (NullPointerException ex) {
            Toast.makeText(FixerTracking.this , "Vui lòng bật GPS rồi thử lại !" , Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

        Map<String, String> content = new HashMap<>();
        content.put("title","Thông báo!");
        content.put("message","Thợ đã hủy yêu cầu");
        DataMessage dataMessage = new DataMessage(token.getToken(), content);
        ifcmService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success == 1){
                    Toast.makeText(FixerTracking.this, "Đã hủy!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Toast.makeText(FixerTracking.this, "The fucking capitalist took the internet... Connection error, cyka blyat!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateCashFee(final Location fixLocation, Location mLastLocation) {
        SubtractAccountFee();
        String requestAPI = null;

        try{
            requestAPI = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + fixLocation.getLatitude() + "," + fixLocation.getLongitude() + "&" +
                    "destination=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            mService.getPath(requestAPI).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try{
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");
                        JSONObject legsObject = legs.getJSONObject(0);
                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");
                        double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));

                        if (Common.service_chose.equals("S ử a   k h ó a   x e   g ắ n   m á y")){
                            fee = Common.motorbyke_lock_service;
                        } else if (Common.service_chose.equals("S ử a   k h ó a   n h à")){
                            fee = Common.house_lock_service;
                        } else if (Common.service_chose.equals("S ử a   k h ó a   x e   h ơ i")){
                            fee = Common.car_lock_service;
                        }

                        sendFixCompleteNotification(customerid);

                        Intent intent = new Intent(FixerTracking.this, ServiceDetailActivity.class);
                        intent.putExtra("start_address", legsObject.getString("start_address"));
                        intent.putExtra("end_address", legsObject.getString("end_address"));
                        intent.putExtra("distance", String.valueOf(distance_value));
                        intent.putExtra("total", Common.formulaPrice(distance_value, fee ));
                        intent.putExtra("location_start",String.format("%f, %f", fixLocation.getLatitude(), fixLocation.getLongitude()));
                        intent.putExtra("location_end",String.format("%f, %f", Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                        //intent.putExtra("services", (Serializable) services);
                        startActivity(intent);
                        finish();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(FixerTracking.this, "Không thể lấy được đường đi, xin vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    System.out.print(t.getMessage());
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void SubtractAccountFee(){
        final double[] fee = new double[1];
        final DatabaseReference fixerInformation = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl).child(Common.FixerID);
        fixerInformation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Fixer fixer = dataSnapshot.getValue(Fixer.class);
                fee[0] = fixer.getJobFee() - 10000;
                UpdateAccountFee(fixerInformation, fee[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void UpdateAccountFee(DatabaseReference databaseReference, final double FeeSubtracted){
        final Map<String, Object> updateinfo = new HashMap<>();
        updateinfo.put("jobFee" , FeeSubtracted);
        databaseReference.updateChildren(updateinfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Common.currentFixer.setJobFee(FeeSubtracted);
                            Log.e("Subtract successful", "This account has been subtracted by 10000 vnd");
                        }
                        else
                            Log.e("Subtract failed", "This account can't be subtracted by 10000 vnd");
                    }
                });
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        ProgressDialog mDialog = new ProgressDialog(FixerTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Chờ tí nhé");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        PolylineOptions polylineOptions = null;
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();
            ArrayList points;
            for (int i =0 ; i < lists.size() ; i++){
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);
                for (int j = 0 ; j < path.size() ; j++){
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(5);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);
            }
            direction = mMap.addPolyline(polylineOptions);
        }
    }
}
