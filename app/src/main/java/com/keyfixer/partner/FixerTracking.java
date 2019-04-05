package com.keyfixer.partner;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Helper.DirectionJSONParser;
import com.keyfixer.partner.Remote.IGoogleAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FixerTracking extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    IGoogleAPI mService;
    double customerlat, customerlng;
    //play services
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleAPiClient;

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
            customerlat = getIntent().getDoubleExtra("lat", -1.0);
            customerlng = getIntent().getDoubleExtra("lng", -1.0);
        }
        mService = Common.getGoogleAPI();
        setupLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        customerMarker = mMap.addCircle(new CircleOptions().center(new LatLng(customerlat, customerlng)).radius(10)
                .strokeColor(Color.BLUE).fillColor(0x220000FF).strokeWidth(5.0f));
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
            Toast.makeText(this, "Bạn bật GPS chưa nhỉ ?!", Toast.LENGTH_SHORT).show();
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

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        ProgressDialog progressDialog = new ProgressDialog(FixerTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Chờ tí nhé");
            progressDialog.show();
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

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            progressDialog.dismiss();
            ArrayList points;
            PolylineOptions polylineOptions = null;
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
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);
            }
            direction = mMap.addPolyline(polylineOptions);
        }
    }
}
