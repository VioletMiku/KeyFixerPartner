package com.keyfixer.partner;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Model.Token;
import com.keyfixer.partner.Remote.IGoogleAPI;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FixerHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener {

    private GoogleMap mMap;
    private IGoogleAPI mService;
    //play services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleAPiClient;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference fixers;
    GeoFire geoFire;
    Marker mCurrent;
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;

    //car animation ... :3
    private List<LatLng> Lo_trinh;
    private Marker carMarker;
    private float v;
    private double lat,lng;
    private Handler handler;
    private LatLng startPosition, endPosition, currentPosition;
    private int index, next;
    private String destination;
    private PolylineOptions polylineOptions, black_polylineOptions;
    private Polyline blackPolyline, grayPolyline;
    private Button btnGo;
    private EditText editText;

    //presense system
    DatabaseReference onlineref, currentUserref;

    //Firebase Storage
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index < Lo_trinh.size()-1){
                index++;
                next = index + 1;
            }
            if (index < Lo_trinh.size()-1){
                startPosition = Lo_trinh.get(index);
                endPosition = Lo_trinh.get(next);
            }

            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try{
                        v = valueAnimator.getAnimatedFraction();
                        lng = v*endPosition.longitude+(1-v)*startPosition.longitude;
                        lat = v*endPosition.latitude +(1-v)*startPosition.latitude;
                        LatLng newPos = new LatLng(lat,lng);
                        carMarker.setPosition(newPos);
                        carMarker.setAnchor(0.5f, 0.5f);
                        carMarker.setRotation(getBearing(startPosition, newPos));
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder().target(newPos).zoom(15.5f).build()
                        ));
                    }catch(NullPointerException NPex){
                        Toast.makeText(FixerHome.this, "NullPointerException!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            valueAnimator.start();
            handler.postDelayed(this, 3000);
        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        if (startPosition.longitude < endPosition.longitude && startPosition.latitude < endPosition.latitude){
            return (float) (Math.toDegrees(Math.atan(lng/lat)));
        }
        else if (startPosition.longitude < endPosition.longitude && startPosition.latitude >= endPosition.latitude){
            return (float) ((90 - Math.toDegrees(Math.atan(lng/lat))) + 90);
        }
        else if (startPosition.longitude >= endPosition.longitude && startPosition.latitude >= endPosition.latitude){
            return (float) (Math.toDegrees(Math.atan(lng/lat)) + 180);
        }
        else if (startPosition.longitude >= endPosition.longitude && startPosition.latitude < endPosition.latitude){
            return (float) ((90 - Math.toDegrees(Math.atan(lng/lat))) + 270);
        }
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixer_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this , drawer , toolbar , R.string.navigation_drawer_open , R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView = navigationView.getHeaderView(0);
        TextView txtName = (TextView) navigationHeaderView.findViewById(R.id.txt_FixerName);
        TextView txtStars = (TextView) navigationHeaderView.findViewById(R.id.txt_Stars);
        CircleImageView imageAvatar = (CircleImageView)navigationHeaderView.findViewById(R.id.image_avatar);

        txtName.setText(Common.currentUser.getStrName());
        txtStars.setText(Common.currentUser.getRates());
        if (Common.currentUser.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentUser.getAvatarUrl())){
            Picasso.with(this).load(Common.currentUser.getAvatarUrl()).into(imageAvatar);
        }

        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Presense system
        onlineref = FirebaseDatabase.getInstance().getReference().child("info/connected");
        currentUserref = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //disconnected fixer from map when they off
                currentUserref.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //init view
        location_switch = (MaterialAnimatedSwitch) findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline){
                    FirebaseDatabase.getInstance().goOnline();//set connected when the fixer comeback
                    startLocationUpdate();
                    displayLocation();
                    Snackbar.make(mapFragment.getView(),"Bạn đang online",Snackbar.LENGTH_SHORT).show();
                }
                else{
                    try{
                        FirebaseDatabase.getInstance().goOffline();//set disconnected when the fixer leave
                        stopLocationUpdate();
                        mCurrent.remove();
                        mMap.clear();
                        handler.removeCallbacks(drawPathRunnable);
                        Snackbar.make(mapFragment.getView(),"Bạn đang offline",Snackbar.LENGTH_SHORT).show();
                    }catch(NullPointerException ex){
                        Toast.makeText(FixerHome.this, "Vui lòng bật GPS rồi thử lại !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //geo fire
        fixers = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl);
        geoFire = new GeoFire(fixers);
        setupLocation();

        mService = Common.getGoogleAPI();
        UpdateFireBaseToken();
    }


    private void UpdateFireBaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);
        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

    private void getDirection() {
        currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
        String requestAPI = null;

        try{
            requestAPI = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + destination + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            Log.d("MikuRoot", requestAPI); //print url to debug
            mService.getPath(requestAPI).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try{
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0 ; i < jsonArray.length() ; i++){
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyLine = poly.getString("points");
                            Lo_trinh = decodePoly(polyLine);
                        }
                        //adjusting bounds
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latlng:Lo_trinh){
                            builder.include(latlng);
                        }
                        LatLngBounds bounds = builder.build();
                        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                        mMap.animateCamera(mCameraUpdate);

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(5);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(Lo_trinh);
                        grayPolyline = mMap.addPolyline(polylineOptions);

                        black_polylineOptions = new PolylineOptions();
                        black_polylineOptions.color(getResources().getColor(R.color.colorAccent));
                        black_polylineOptions.width(5);
                        black_polylineOptions.startCap(new SquareCap());
                        black_polylineOptions.endCap(new SquareCap());
                        black_polylineOptions.jointType(JointType.ROUND);
                        blackPolyline = mMap.addPolyline(black_polylineOptions);

                        mMap.addMarker(new MarkerOptions()
                                .position(Lo_trinh.get(Lo_trinh.size() - 1))
                                .title("Nơi sửa"));

                        //Animation
                        ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                        polyLineAnimator.setDuration(2000);
                        polyLineAnimator.setInterpolator(new LinearInterpolator());
                        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                List<LatLng> points = grayPolyline.getPoints();
                                int percentValue = (int)animation.getAnimatedValue();
                                int size = points.size();
                                int newPoints = (int)(size * (percentValue/100.0f));
                                List<LatLng> point = points.subList(0, newPoints);
                                blackPolyline.setPoints(point);
                            }
                        });
                        polyLineAnimator.start();

                        carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                        handler = new Handler();
                        index = -1;
                        next = 1;
                        handler.postDelayed(drawPathRunnable, 3000);

                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(FixerHome.this, "Không thể lấy được đường đi, xin vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    System.out.print(t.getMessage());
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (checkPlaySerives()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        if (location_switch.isChecked()){
                            displayLocation();
                        }
                    }
                }
                break;
        }
    }

    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else{
            if (checkPlaySerives()){
                buildGoogleApiClient();
                createLocationRequest();
                if (location_switch.isChecked()){
                    displayLocation();
                }
            }
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

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPiClient, mLocationRequest, this);
    }

    private void stopLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleAPiClient,this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleAPiClient);
        if (Common.mLastLocation != null){//co bug cho nay _ mlastLocation = null
            if (location_switch.isChecked()){
                final double latitude = Common.mLastLocation.getLatitude();
                final double longtitude = Common.mLastLocation.getLongitude();
                //Update to firebase
                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longtitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Add marker
                        if (mCurrent != null){
                            mCurrent.remove(); //remove already marker
                        }
                        mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longtitude)).title("Bạn"));
                        //Move camera to this position
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longtitude),15.0f));

                    }
                });
            }
        }
        else{
            Log.d("Ối!", "Không thể xác định được vị trí của fixer");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fixer_home , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_fix_history) {
            // Handle the camera action
        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_signout) {
            Signout();
        } else if (id == R.id.nav_view) {

        } else if (id == R.id.nav_way_bill) {

        } else if (id == R.id.nav_update_information) {
            ShowDialogUpdateInfo();
        } else if (id == R.id.nav_change_password) {
            showDialogChangePassword();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void ShowDialogUpdateInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FixerHome.this);
        alertDialog.setTitle("Cập nhật thông tin");
        alertDialog.setMessage("Vui lòng cung cấp đầy đủ thông tin");
        LayoutInflater inflater = this.getLayoutInflater();
        View layout_update_info = inflater.inflate(R.layout.layout_update_information, null);
        final MaterialEditText edtName = (MaterialEditText) layout_update_info.findViewById(R.id.edt_Name);
        final MaterialEditText edtPhone = (MaterialEditText) layout_update_info.findViewById(R.id.edt_Phone);
        final ImageView image_upload = (ImageView) layout_update_info.findViewById(R.id.image_upload);
        image_upload.setOnClickListener(this);
        alertDialog.setView(layout_update_info);

        alertDialog.setPositiveButton("Cập nhật" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface , int i) {
                dialogInterface.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog(FixerHome.this);
                waitingDialog.show();

                String name = edtName.getText().toString();
                String phone = edtPhone.getText().toString();

                Map<String, Object> updateinfo = new HashMap<>();
                if (!TextUtils.isEmpty(name))
                    updateinfo.put("strName", name);
                if (!TextUtils.isEmpty(phone))
                    updateinfo.put("strPhone", phone);

                DatabaseReference fixerInformation = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
                fixerInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(updateinfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    Toast.makeText(FixerHome.this , "Thông tin cập nhật hoàn tất" , Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(FixerHome.this , "Cập nhật thông tin thất bại!" , Toast.LENGTH_SHORT).show();

                                waitingDialog.dismiss();
                            }
                        });
            }
        });
        alertDialog.setNegativeButton("Hủy" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface , int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void showDialogChangePassword() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(FixerHome.this);
        alertDialog.setTitle("Thay đổi mật khẩu");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_change_password, null);
        final MaterialEditText edtOldPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edt_OldPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edt_NewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edt_RepeatPassword);

        alertDialog.setView(layout_pwd);
        alertDialog.setPositiveButton("Đổi mật khẩu" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface , int i) {
                final SpotsDialog waitingDialog = new SpotsDialog(FixerHome.this);
                waitingDialog.show();

                if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString())){

                    String strEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    AuthCredential credential = EmailAuthProvider.getCredential(strEmail, edtOldPassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                FirebaseAuth.getInstance().getCurrentUser().updatePassword(edtRepeatPassword.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){

                                                    Map<String, Object> password = new HashMap<>();
                                                    password.put("password", edtRepeatPassword.getText().toString());
                                                    DatabaseReference fixerInf = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
                                                    fixerInf.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(password)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        Toast.makeText(FixerHome.this , "Mật khẩu thay đổi thành công" , Toast.LENGTH_SHORT).show();         
                                                                    } else{
                                                                        Toast.makeText(FixerHome.this , "Mật khẩu đã thay đổi nhưng chưa hoàn tất việc cập nhật" , Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    waitingDialog.dismiss();
                                                                }
                                                            });

                                                } else{
                                                    Toast.makeText(FixerHome.this , "Mật khẩu thay đổi thất bại" , Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                            } else{
                                waitingDialog.dismiss();
                                Toast.makeText(FixerHome.this , "Sai mật khẩu" , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else{
                    Toast.makeText(FixerHome.this , "Mật khẩu lặp lại không trùng khớp với mật khẩu mới" , Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.setNegativeButton("Hủy" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface , int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void Signout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(FixerHome.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.image_upload:
                chooseImage();
                break;
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn hình đại diện..."), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent data) {
        super.onActivityResult(requestCode , resultCode , data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri saveuri = data.getData();
            if (saveuri != null){
                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Đang tải ... ");
                mDialog.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = storageReference.child("images/" + imageName);
                imageFolder.putFile(saveuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mDialog.dismiss();
                        imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Toast.makeText(FixerHome.this , "Đang tải ... " , Toast.LENGTH_SHORT).show();
                                Map<String, Object> avatarUpdate = new HashMap<>();
                                avatarUpdate.put("avatarUrl", uri.toString());
                                DatabaseReference fixerInformation = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
                                fixerInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(avatarUpdate)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())

                                                    Toast.makeText(FixerHome.this , "Tải hoàn tất" , Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(FixerHome.this , "Tải thất bại" , Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mDialog.setMessage("Đã tải được " + progress + "%");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
