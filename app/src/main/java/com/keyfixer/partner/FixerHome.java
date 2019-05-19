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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.keyfixer.partner.Adapter.ListViewCustomAdapter_forNonActivatedAccount;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Model.Fixer;
import com.keyfixer.partner.Model.Token;
import com.keyfixer.partner.Remote.IGoogleAPI;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FixerHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
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

    DatabaseReference house_service_location, car_service_location, bike_service_location;
    GeoFire geoFire_for_house_service, geoFire_for_car_service, geoFire_for_bike_service;
    //MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;
    ImageView gpson, gpsoff;
    TextView txtAccountFee;

    //car animation ... :3
    private List<LatLng> Lo_trinh;
    private Marker carMarker;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition, currentPosition;
    private int index, next;
    private String destination, activated;
    private List<Fixer> ActivatedList = null, nonActivatedList = null;
    private PolylineOptions polylineOptions, black_polylineOptions;
    private Polyline blackPolyline, grayPolyline;
    private Button btnGo;
    private EditText editText;
    RelativeLayout slider;

    //presense system
    DatabaseReference onlineref, currentUserref;

    //Firebase Storage
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    //bottom sheet
    CoordinatorLayout layout_bottom_sheet;
    BottomSheetBehavior bottom_sheet_behavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixer_home);

        layout_bottom_sheet = (CoordinatorLayout) findViewById(R.id.bottom_sheet);
        bottom_sheet_behavior = BottomSheetBehavior.from(layout_bottom_sheet);
        txtAccountFee = (TextView) findViewById(R.id.alert_account_fee);
        txtAccountFee.setText("$" + Common.currentFixer.getJobFee());

        slider = (RelativeLayout) findViewById(R.id.slider);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        Common.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this , drawer , toolbar , R.string.navigation_drawer_open , R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        slider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.addDrawerListener(toggle);
                toggle.syncState();
            }
        });
        //menu options
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Common.isAdmin = Common.currentFixer.isAdmin();
        if (Common.currentFixer.getJobFee() <= 0){
            Common.isActivated = false;
            Toast.makeText(this , "Tài khoản vừa hết tiền" , Toast.LENGTH_SHORT).show();
        }
        if (Common.isAdmin){
            toggleVisibility(navigationView.getMenu(), R.id.nav_provide_admin_rights, true);
            toggleVisibility(navigationView.getMenu(), R.id.nav_fixer_account_activate, true);
            toggleVisibility(navigationView.getMenu(), R.id.nav_statistical_management, true);
            toggleVisibility(navigationView.getMenu(), R.id.nav_fix_history, false);
        }
        if (Common.isActivated)
            activated = "activated";
        else
            activated = "not activated";

        if (Common.currentFixer.isCanFixHouseKey()) {
            Common.used_service = "S ử a   k h ó a   n h à";
        } else{
            Common.used_service = "";
        }
        if (Common.currentFixer.isCanFixCarKey()) {
            Common.used_service1 = "S ử a   k h ó a   x e   h ơ i";
        } else{
            Common.used_service1 = "";
        }
        if (Common.currentFixer.isCanFixBikeKey()) {
            Common.used_service2 = "S ử a   k h ó a   x e   g ắ n   m á y";
        } else{
            Common.used_service2 = "";
        }

        View navigationHeaderView = navigationView.getHeaderView(0);
        TextView txtName = (TextView) navigationHeaderView.findViewById(R.id.txt_FixerName);
        TextView txtStars = (TextView) navigationHeaderView.findViewById(R.id.txt_Stars);
        CircleImageView imageAvatar = (CircleImageView) navigationHeaderView.findViewById(R.id.image_avatar);

        txtName.setText(Common.currentFixer.getStrName());
        txtStars.setText(Common.currentFixer.getRates());
        if (Common.currentFixer.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentFixer.getAvatarUrl())) {
            Picasso.with(this).load(Common.currentFixer.getAvatarUrl()).into(imageAvatar);
        }

        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Presense system
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                onlineref = FirebaseDatabase.getInstance().getReference().child("info/connected");
                if (Common.currentFixer.isCanFixHouseKey()){
                    currentUserref = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl)
                            .child("S ử a   k h ó a   n h à")
                            .child(activated)
                            .child(account.getId());
                    Log.e("Current user references", "" + currentUserref);
                } else {
                    currentUserref = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl)
                            .child("S ử a   k h ó a   n h à")
                            .child(activated)
                            .child(account.getId());
                    currentUserref.setValue(null);
                    Log.e("Current user references", "" + currentUserref + " has been removed");
                }
                if (Common.currentFixer.isCanFixCarKey()){
                    currentUserref = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl)
                            .child("S ử a   k h ó a   x e   h ơ i")
                            .child(activated)
                            .child(account.getId());
                    Log.e("Current user references", "" + currentUserref);
                } else {
                    currentUserref = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl)
                            .child("S ử a   k h ó a   x e   h ơ i")
                            .child(activated)
                            .child(account.getId());
                    currentUserref.setValue(null);
                    Log.e("Current user references", "" + currentUserref + " has been removed");
                }
                if (Common.currentFixer.isCanFixBikeKey()){
                    currentUserref = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl)
                            .child("S ử a   k h ó a   x e   g ắ n   m á y")
                            .child(activated)
                            .child(account.getId());
                    Log.e("Current user references", "" + currentUserref);
                } else {
                    currentUserref = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl)
                            .child("S ử a   k h ó a   x e   g ắ n   m á y")
                            .child(activated)
                            .child(account.getId());
                    currentUserref.setValue(null);
                    Log.e("Current user references", "" + currentUserref + " has been removed");
                }
                Common.currentUserref = currentUserref;
                Common.FixerID = account.getId();
                onlineref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //disconnected fixer from map when they off
                        Common.currentUserref.onDisconnect().removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });
        //init view
        gpson = (ImageView) findViewById(R.id.gps_on);
        gpsoff = (ImageView) findViewById(R.id.gps_off);
        //location_switch = (MaterialAnimatedSwitch) findViewById(R.id.location_switch);
        gpsoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsoff.setVisibility(View.INVISIBLE);
                gpson.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().goOnline();//set connected when the fixer comeback
                buildLocationCallback();
                buildLocationRequest();
                if (ActivityCompat.checkSelfPermission(FixerHome.this , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(FixerHome.this , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Common.fusedLocationProviderClient.requestLocationUpdates(mLocationRequest , Common.locationCallback , Looper.myLooper());

                //geo fire
                if (Common.currentFixer.isCanFixHouseKey()){
                    house_service_location = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).child("S ử a   k h ó a   n h à").child(activated);
                    geoFire_for_house_service = new GeoFire(house_service_location);
                }
                if (Common.currentFixer.isCanFixCarKey()){
                    car_service_location = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).child("S ử a   k h ó a   x e   h ơ i").child(activated);
                    geoFire_for_car_service = new GeoFire(car_service_location);
                }
                if (Common.currentFixer.isCanFixBikeKey()){
                    bike_service_location = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).child("S ử a   k h ó a   x e   g ắ n   m á y").child(activated);
                    geoFire_for_bike_service = new GeoFire(bike_service_location);
                }

                displayLocation();
                Snackbar.make(mapFragment.getView() , "Bạn đang online" , Snackbar.LENGTH_SHORT).show();
            }
        });
        gpson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpson.setVisibility(View.INVISIBLE);
                gpsoff.setVisibility(View.VISIBLE);
                try {
                    FirebaseDatabase.getInstance().goOffline();//set disconnected when the fixer leave
                    Common.fusedLocationProviderClient.removeLocationUpdates(Common.locationCallback);
                    Common.mCurrent.remove();
                    mMap.clear();
                    if (handler != null)
                        handler.removeCallbacks(drawPathRunnable);
                    Snackbar.make(mapFragment.getView() , "Bạn đang offline" , Snackbar.LENGTH_SHORT).show();
                } catch (NullPointerException ex) {
                    Toast.makeText(FixerHome.this , "Vui lòng bật GPS rồi thử lại !" , Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*
        * location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline){
                    FirebaseDatabase.getInstance().goOnline();//set connected when the fixer comeback
                    buildLocationCallback();
                    buildLocationRequest();
                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
                    displayLocation();
                    Snackbar.make(mapFragment.getView(),"Bạn đang online",Snackbar.LENGTH_SHORT).show();
                }
                else{
                    try{
                        FirebaseDatabase.getInstance().goOffline();//set disconnected when the fixer leave
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                        mCurrent.remove();
                        mMap.clear();
                        if (handler != null)
                            handler.removeCallbacks(drawPathRunnable);
                        Snackbar.make(mapFragment.getView(),"Bạn đang offline",Snackbar.LENGTH_SHORT).show();
                    }catch(NullPointerException ex){
                        Toast.makeText(FixerHome.this, "Vui lòng bật GPS rồi thử lại !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        * */
        setupLocation();

        mService = Common.getGoogleAPI();
        UpdateFireBaseToken();
    }

    private void UpdateFireBaseToken() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference tokens = db.getReference(Common.token_tbl);
                Token token = new Token(FirebaseInstanceId.getInstance().getToken());
                tokens.child(account.getId()).setValue(token);
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });
    }

    private void getDirection() {
        currentPosition = new LatLng(Common.mLastLocation.getLatitude() , Common.mLastLocation.getLongitude());
        String requestAPI = null;

        try {
            requestAPI = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + destination + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            Log.d("MikuRoot" , requestAPI); //print url to debug
            mService.getPath(requestAPI).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call , Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyLine = poly.getString("points");
                            Lo_trinh = decodePoly(polyLine);
                        }
                        //adjusting bounds
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latlng : Lo_trinh) {
                            builder.include(latlng);
                        }
                        LatLngBounds bounds = builder.build();
                        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds , 2);
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
                        ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0 , 100);
                        polyLineAnimator.setDuration(2000);
                        polyLineAnimator.setInterpolator(new LinearInterpolator());
                        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                List<LatLng> points = grayPolyline.getPoints();
                                int percentValue = (int) animation.getAnimatedValue();
                                int size = points.size();
                                int newPoints = (int) (size * (percentValue / 100.0f));
                                List<LatLng> point = points.subList(0 , newPoints);
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
                        handler.postDelayed(drawPathRunnable , 3000);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call , Throwable t) {
                    Toast.makeText(FixerHome.this , "Không thể lấy được đường đi, xin vui lòng thử lại!" , Toast.LENGTH_SHORT).show();
                    System.out.print(t.getMessage());
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
/*
*
    @Override
    protected void onStop() {

        FirebaseDatabase.getInstance().goOffline();//set disconnected when the fixer leave
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        mCurrent.remove();
        mMap.clear();
        if (handler != null)
            handler.removeCallbacks(drawPathRunnable);
        super.onStop();
    }
* */

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

            LatLng p = new LatLng((((double) lat / 1E5)) ,
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void toggleVisibility(Menu menu, @IdRes int id, boolean visible){
        menu.findItem(id).setVisible(visible);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode , @NonNull String[] permissions , @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildLocationCallback();
                    buildLocationRequest();
                    if (gpson.getVisibility() == View.VISIBLE) {
                        displayLocation();
                    }
                }
                break;
        }
    }

    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(this , android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this , android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request runtime permission
            ActivityCompat.requestPermissions(this , new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION ,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            } , MY_PERMISSION_REQUEST_CODE);
        } else {
            buildLocationCallback();
            buildLocationRequest();
            if (gpson.getVisibility() == View.VISIBLE) {
                //geo fire
                if (Common.currentFixer.isCanFixHouseKey()){
                    house_service_location = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).child("S ử a   k h ó a   n h à").child(activated);
                    geoFire_for_house_service = new GeoFire(house_service_location);
                }
                if (Common.currentFixer.isCanFixCarKey()){
                    car_service_location = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).child("S ử a   k h ó a   x e   h ơ i").child(activated);
                    geoFire_for_car_service = new GeoFire(car_service_location);
                }
                if (Common.currentFixer.isCanFixBikeKey()) {
                    bike_service_location = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).child("S ử a   k h ó a   x e   g ắ n   m á y").child(activated);
                    geoFire_for_bike_service = new GeoFire(bike_service_location);
                }

                displayLocation();
            }
        }
    }

    private void buildLocationCallback() {
        Common.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations())
                    Common.mLastLocation = location;
                displayLocation();
            }
        };
    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this , android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Common.fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Common.mLastLocation = location;
                if (Common.mLastLocation != null) {//co bug cho nay _ mlastLocation = null
                    if (gpson.getVisibility() == View.VISIBLE) {
                        final double latitude = Common.mLastLocation.getLatitude();
                        final double longtitude = Common.mLastLocation.getLongitude();
                        //Update to firebase
                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                            @Override
                            public void onSuccess(Account account) {
                                if (Common.currentFixer.isCanFixHouseKey()){
                                    geoFire_for_house_service.setLocation(account.getId() , new GeoLocation(latitude , longtitude) , new GeoFire.CompletionListener() {
                                        @Override
                                        public void onComplete(String key , DatabaseError error) {
                                            //Add marker
                                            if (Common.mCurrent != null) {
                                                Common.mCurrent.remove(); //remove already marker
                                            }
                                            Common.mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude , longtitude)).title("Bạn"));
                                            //Move camera to this position
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude , longtitude) , 15.0f));

                                        }
                                    });
                                }
                                if (Common.currentFixer.isCanFixCarKey()){
                                    geoFire_for_car_service.setLocation(account.getId() , new GeoLocation(latitude , longtitude) , new GeoFire.CompletionListener() {
                                        @Override
                                        public void onComplete(String key , DatabaseError error) {
                                            //Add marker
                                            if (Common.mCurrent != null) {
                                                Common.mCurrent.remove(); //remove already marker
                                            }
                                            Common.mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude , longtitude)).title("Bạn"));
                                            //Move camera to this position
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude , longtitude) , 15.0f));

                                        }
                                    });
                                }
                                if (Common.currentFixer.isCanFixBikeKey()){
                                    geoFire_for_bike_service.setLocation(account.getId() , new GeoLocation(latitude , longtitude) , new GeoFire.CompletionListener() {
                                        @Override
                                        public void onComplete(String key , DatabaseError error) {
                                            //Add marker
                                            if (Common.mCurrent != null) {
                                                Common.mCurrent.remove(); //remove already marker
                                            }
                                            Common.mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude , longtitude)).title("Bạn"));
                                            //Move camera to this position
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude , longtitude) , 15.0f));

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(AccountKitError accountKitError) {

                            }
                        });
                    }
                } else {
                    Log.d("Ối!" , "Không thể xác định được vị trí của fixer");
                }
            }
        });
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


    private void ActiveSuccess(){
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Kích hoạt thành công!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void ActiveFailed(){
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Kích hoạt thất bại!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void AddAdminRuleSuccess(){
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Thêm quyền thành công!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void AddAdminRuleFailed(){
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Không thể thêm quyền!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void UpdateSuccess(){
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Cập nhật thành công!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void UpdateFailed(){
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Cập nhật thất bại!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void AlertUser(){
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Vui lòng tái kích hoạt GPS trên thiết bị để cập nhật lại dịch vụ");
        dialog.setCancelable(true);
        dialog.show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_fix_history) {
            Intent history = new Intent(FixerHome.this, Main2Activity.class);
            startActivity(history);
        } else if (id == R.id.nav_statistical_management) {
            Intent intent2 = new Intent(FixerHome.this, StatisticalManagementActivity.class);
            startActivity(intent2);
        } else if (id == R.id.nav_servicetype) {
            ShowDialogupdateServiceType();
        } else if (id == R.id.nav_signout) {
            Signout();
        } else if (id == R.id.nav_update_information) {
            ShowDialogUpdateInfo();
        } else if (id == R.id.nav_fixer_account_activate) {
            getNonActivatedList();
        } else if (id == R.id.nav_provide_admin_rights) {
            getActivatedList();
        } else if (id == R.id.nav_fix_home) {
            Intent home = new Intent(FixerHome.this, FixerHome.class);
            startActivity(home);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void ShowNotActivatedListDialog(final List<Fixer> list) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(FixerHome.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View nonactivated_account = inflater.inflate(R.layout.layout_fixerlist_waiting_for_check , null);

        final CircleImageView avatar = (CircleImageView) nonactivated_account.findViewById(R.id.personal_avatar);
        final TextView personal_name = (TextView) nonactivated_account.findViewById(R.id.personal_name);
        final TextView personal_email = (TextView) nonactivated_account.findViewById(R.id.personal_email);
        final ProgressBar progressBar = (ProgressBar) nonactivated_account.findViewById(R.id.progressBar);
        final ListView listView_nonActivatedAccount = (ListView) nonactivated_account.findViewById(R.id.non_activated_account_list);
        final ListViewCustomAdapter_forNonActivatedAccount adapter;

        if (Common.currentFixer != null){
            if (Common.currentFixer.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentFixer.getAvatarUrl())){
                Picasso.with(this).load(Common.currentFixer.getAvatarUrl()).into(avatar);
            }
            personal_name.setText(Common.currentFixer.getStrName());
            personal_email.setText(Common.currentFixer.getStrEmail());
        }

        if (list != null){
            adapter = new ListViewCustomAdapter_forNonActivatedAccount(this, R.layout.layout_custom_listview_nonactivated_account, list);
            listView_nonActivatedAccount.setAdapter(adapter);
            listView_nonActivatedAccount.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent , View view , int position , long id) {
                    Fixer fixer = list.get(position);
                    ActiveAccount(fixer, list, listView_nonActivatedAccount);
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
        }

        alertDialog.setView(nonactivated_account);

        alertDialog.setPositiveButton("Hoàn tất" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface , int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void ShowActivatedListDialog(final List<Fixer> list) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FixerHome.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View nonactivated_account = inflater.inflate(R.layout.layout_add_admin_rule , null);

        final CircleImageView avatar = (CircleImageView) nonactivated_account.findViewById(R.id.personal_avatar_);
        final TextView personal_name = (TextView) nonactivated_account.findViewById(R.id.personal_name_);
        final TextView personal_email = (TextView) nonactivated_account.findViewById(R.id.personal_email_);
        final ProgressBar progressBar = (ProgressBar) nonactivated_account.findViewById(R.id.progressBar_);
        final ListView listView_nonActivatedAccount = (ListView) nonactivated_account.findViewById(R.id.activated_account_list);
        final ListViewCustomAdapter_forNonActivatedAccount adapter;

        if (Common.currentFixer != null){
            if (Common.currentFixer.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentFixer.getAvatarUrl())){
                Picasso.with(FixerHome.this).load(Common.currentFixer.getAvatarUrl()).into(avatar);
            }
            personal_name.setText(Common.currentFixer.getStrName());
            personal_email.setText(Common.currentFixer.getStrEmail());
        }

        if (list != null){
            adapter = new ListViewCustomAdapter_forNonActivatedAccount(FixerHome.this, R.layout.layout_custom_listview_nonactivated_account, list);
            listView_nonActivatedAccount.setAdapter(adapter);
            listView_nonActivatedAccount.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent , View view , int position , long id) {
                    Fixer fixer = list.get(position);
                    AddAdminRuleAccount(fixer);
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
        }

        alertDialog.setView(nonactivated_account);

        alertDialog.setPositiveButton("Hoàn tất" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface , int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    ////////////////////////      Cần hoàn tất hàm này      ////////////////////////////////////////
    private List<Fixer> getActivatedList() {
        ActivatedList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot item:snapshots){
                        Fixer fixer = item.getValue(Fixer.class);
                        if (fixer.isActivated() && !fixer.isAdmin())
                            ActivatedList.add(fixer);
                    }
                    ShowActivatedListDialog(ActivatedList);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Exception", "Error: ", databaseError.toException());
            }
        });
        return ActivatedList;
    }

    ////////////////////////      Cần hoàn tất hàm này      ////////////////////////////////////////
    private List<Fixer> getNonActivatedList(){
        nonActivatedList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot item:snapshots){
                        Fixer fixer = item.getValue(Fixer.class);
                        if (!fixer.isActivated())
                            nonActivatedList.add(fixer);
                    }
                    ShowNotActivatedListDialog(nonActivatedList);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Exception", "Error: ", databaseError.toException());
            }
        });
        return nonActivatedList;
    }

    private void ActiveAccount(final Fixer model, final List<Fixer> fixerList, final ListView listView){
        final Map<String, Object> updateinfo = new HashMap<>();
        updateinfo.put("activated" , true);

        final DatabaseReference fixer_tbl = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
        fixer_tbl.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren(); // lấy danh sách fixer trong bảng fixer
                for (DataSnapshot item:snapshots){
                    final Fixer fixer = item.getValue(Fixer.class); // lấy từng fixer trong danh sách fixer lấy được
                    if (fixer.getStrPhone().equals(model.getStrPhone())){
                        fixer_tbl.child(item.getKey()).updateChildren(updateinfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    fixerList.remove(fixer);
                                    ListViewCustomAdapter_forNonActivatedAccount adapter =
                                            new ListViewCustomAdapter_forNonActivatedAccount(FixerHome.this, R.layout.layout_custom_listview_nonactivated_account, fixerList);
                                    listView.setAdapter(adapter);
                                    ActiveSuccess();
                                }
                                else
                                    ActiveFailed();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void AddAdminRuleAccount(final Fixer model) {
        final Map<String, Object> updateinfo = new HashMap<>();
        updateinfo.put("admin" , true);

        final DatabaseReference fixerInformation = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
        fixerInformation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                for (DataSnapshot item:snapshots){
                    Fixer fixer = item.getValue(Fixer.class);
                    if (fixer.getStrPhone().equals(model.getStrPhone())){
                        fixerInformation.child(item.getKey()).updateChildren(updateinfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    AddAdminRuleSuccess();
                                else
                                    AddAdminRuleFailed();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ShowDialogupdateServiceType() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FixerHome.this);
        alertDialog.setTitle("Cập nhật loại dịch vụ");
        alertDialog.setMessage("Chọn loại dịch vụ bạn có thể sửa");
        LayoutInflater inflater = this.getLayoutInflater();
        View service_type = inflater.inflate(R.layout.layout_update_service_type , null);

        final CheckBox checkboxHome = (CheckBox) service_type.findViewById(R.id.checkbox_house);
        final CheckBox checkboxCar = (CheckBox) service_type.findViewById(R.id.checkbox_car);
        final CheckBox checkboxMotorbike = (CheckBox) service_type.findViewById(R.id.checkbox_motorbike);

        if (Common.currentFixer.isCanFixHouseKey()) {
            checkboxHome.setChecked(true);
            Common.used_service = "S ử a   k h ó a   n h à";
        }
        if (Common.currentFixer.isCanFixCarKey()) {
            checkboxCar.setChecked(true);
            Common.used_service1 = "S ử a   k h ó a   x e   h ơ i";
        }
        if (Common.currentFixer.isCanFixBikeKey()) {
            checkboxMotorbike.setChecked(true);
            Common.used_service2 = "S ử a   k h ó a   x e   g ắ n   m á y";
        }

        if(!Common.currentFixer.isCanFixHouseKey()){
            checkboxHome.setChecked(false);
            Common.used_service = "";

            try{
                DatabaseReference homeservice = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).getRoot().child("S ử a   k h ó a   nhà").child(Common.FixerID);
                homeservice.setValue(null);
            }catch (Exception ex){
                Log.e("Exception house", "" + ex.getMessage() + " \n-- caused by: " + ex.getCause());
            }
        }
        if(!Common.currentFixer.isCanFixCarKey()){
            checkboxCar.setChecked(false);
            Common.used_service1 = "";

            try{
                DatabaseReference carservice = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).getRoot().child("S ử a   k h ó a   x e   h ơ i").child(Common.FixerID);
                carservice.setValue(null);
            }catch (Exception ex){
                Log.e("Exception house", "" + ex.getMessage() + " \n-- caused by: " + ex.getCause());
            }
        }
        if(!Common.currentFixer.isCanFixBikeKey()){
            checkboxMotorbike.setChecked(false);
            Common.used_service2 = "";

            try{
                DatabaseReference bikeservice = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl).getRoot().child("S ử a   k h ó a   x e   g ắ n   m á y").child(Common.FixerID);
                bikeservice.setValue(null);
            }catch (Exception ex){
                Log.e("Exception house", "" + ex.getMessage() + " \n-- caused by: " + ex.getCause());
            }
        }

        alertDialog.setView(service_type);

        alertDialog.setPositiveButton("Cập nhật" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface , int i) {
                dialogInterface.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog(FixerHome.this);
                waitingDialog.show();

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(final Account account) {

                        Map<String, Object> updateinfo = new HashMap<>();
                        if (checkboxHome.isChecked())
                            updateinfo.put("canFixHouseKey", true);
                        else
                            updateinfo.put("canFixHouseKey", false);
                        if (checkboxCar.isChecked())
                            updateinfo.put("canFixCarKey", true);
                        else
                            updateinfo.put("canFixCarKey", false);
                        if (checkboxMotorbike.isChecked())
                            updateinfo.put("canFixBikeKey", true);
                        else
                            updateinfo.put("canFixBikeKey", false);
                        DatabaseReference fixerInformation = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
                        fixerInformation.child(account.getId())
                                .updateChildren(updateinfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            if (Common.currentFixer.isCanFixHouseKey()){
                                                currentUserref = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl)
                                                        .child("S ử a   k h ó a   n h à")
                                                        .child(activated)
                                                        .child(account.getId());
                                            }
                                            if (Common.currentFixer.isCanFixCarKey()){
                                                currentUserref = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl)
                                                        .child("S ử a   k h ó a   x e   h ơ i")
                                                        .child(activated)
                                                        .child(account.getId());
                                            }
                                            if (Common.currentFixer.isCanFixBikeKey()){
                                                currentUserref = FirebaseDatabase.getInstance().getReference(Common.fixer_tbl)
                                                        .child("S ử a   k h ó a   x e   g ắ n   m á y")
                                                        .child(activated)
                                                        .child(account.getId());
                                            }
                                            //Toast.makeText(FixerHome.this , "Thông tin cập nhật hoàn tất" , Toast.LENGTH_SHORT).show();
                                            Log.e("Choose servie alert" , "Choose successful");
                                            AlertUser();
                                        }
                                        else
                                            //Toast.makeText(FixerHome.this , "Cập nhật thông tin thất bại!" , Toast.LENGTH_SHORT).show();
                                            Log.e("Choose servie alert", "Choose failed");

                                        waitingDialog.dismiss();
                                    }
                                });

                        fixerInformation.child(account.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Common.currentFixer = dataSnapshot.getValue(Fixer.class);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

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

    private void ShowDialogUpdateInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FixerHome.this);
        alertDialog.setTitle("Cập nhật thông tin");
        alertDialog.setMessage("Vui lòng cung cấp đầy đủ thông tin");
        LayoutInflater inflater = this.getLayoutInflater();
        View layout_update_info = inflater.inflate(R.layout.layout_update_information , null);
        final MaterialEditText edtName = (MaterialEditText) layout_update_info.findViewById(R.id.edt_Name);
        final MaterialEditText edtPhone = (MaterialEditText) layout_update_info.findViewById(R.id.edt_Phone);
        final MaterialEditText edtemail = (MaterialEditText) layout_update_info.findViewById(R.id.edt_email);
        final ImageView image_upload = (ImageView) layout_update_info.findViewById(R.id.image_upload);
        image_upload.setOnClickListener(this);
        alertDialog.setView(layout_update_info);

        alertDialog.setPositiveButton("Cập nhật" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface , int i) {
                dialogInterface.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog(FixerHome.this);
                waitingDialog.show();

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        String name = edtName.getText().toString();
                        String phone = edtPhone.getText().toString();
                        String email = edtemail.getText().toString();

                        Map<String, Object> updateinfo = new HashMap<>();
                        if (!TextUtils.isEmpty(name)) {
                            updateinfo.put("strName", name);
                            Common.currentFixer.setStrName(name);
                        }
                        if (!TextUtils.isEmpty(phone)) {
                            updateinfo.put("strPhone", phone);
                            Common.currentFixer.setStrPhone(phone);
                        }
                        if (!TextUtils.isEmpty(email)) {
                            updateinfo.put("strEmail", email);
                            Common.currentFixer.setStrEmail(email);
                        }

                        DatabaseReference fixerInformation = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
                        fixerInformation.child(account.getId())
                                .updateChildren(updateinfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            UpdateSuccess();
                                        else
                                            UpdateFailed();

                                        waitingDialog.dismiss();
                                    }
                                });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

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

    private void Signout() {
        android.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new android.app.AlertDialog.Builder(this , android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new android.app.AlertDialog.Builder(this);

        builder.setMessage("Thật sự muốn thoát!?").setPositiveButton(android.R.string.ok , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog , int which) {
                AccountKit.logOut();
                Intent intent = new Intent(FixerHome.this , MainActivity.class);
                startActivity(intent);
                finish();
            }
        }).setNegativeButton(android.R.string.cancel , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog , int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_upload:
                chooseImage();
                break;
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent , "Chọn hình đại diện...") , Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent data) {
        super.onActivityResult(requestCode , resultCode , data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri saveuri = data.getData();
            if (saveuri != null) {
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
                            public void onSuccess(final Uri uri) {
                                Toast.makeText(FixerHome.this , "Đang tải ... " , Toast.LENGTH_SHORT).show();
                                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                    @Override
                                    public void onSuccess(Account account) {
                                        Map<String, Object> avatarUpdate = new HashMap<>();
                                        avatarUpdate.put("avatarUrl" , uri.toString());
                                        DatabaseReference fixerInformation = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
                                        fixerInformation.child(account.getId()).updateChildren(avatarUpdate)
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

                                    @Override
                                    public void onError(AccountKitError accountKitError) {

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //Update location
        buildLocationCallback();
        buildLocationRequest();
        if (ActivityCompat.checkSelfPermission(FixerHome.this , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(FixerHome.this , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Common.fusedLocationProviderClient.requestLocationUpdates(mLocationRequest , Common.locationCallback , Looper.myLooper());
    }

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index < Lo_trinh.size() - 1) {
                index++;
                next = index + 1;
            }
            if (index < Lo_trinh.size() - 1) {
                startPosition = Lo_trinh.get(index);
                endPosition = Lo_trinh.get(next);
            }

            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0 , 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        v = valueAnimator.getAnimatedFraction();
                        lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                        lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                        LatLng newPos = new LatLng(lat , lng);
                        carMarker.setPosition(newPos);
                        carMarker.setAnchor(0.5f , 0.5f);
                        carMarker.setRotation(getBearing(startPosition , newPos));
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder().target(newPos).zoom(15.5f).build()
                        ));
                    } catch (NullPointerException NPex) {
                        Toast.makeText(FixerHome.this , "NullPointerException!" , Toast.LENGTH_SHORT).show();
                    }
                }
            });
            valueAnimator.start();
            handler.postDelayed(this , 3000);
        }
    };

    private float getBearing(LatLng startPosition , LatLng endPosition) {
        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        if (startPosition.longitude < endPosition.longitude && startPosition.latitude < endPosition.latitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        } else if (startPosition.longitude < endPosition.longitude && startPosition.latitude >= endPosition.latitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        } else if (startPosition.longitude >= endPosition.longitude && startPosition.latitude >= endPosition.latitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        } else if (startPosition.longitude >= endPosition.longitude && startPosition.latitude < endPosition.latitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        }
        return -1;
    }

}
