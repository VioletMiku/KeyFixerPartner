package com.keyfixer.partner;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.keyfixer.partner.Adapter.CustomListView_Statistical;
import com.keyfixer.partner.Adapter.ListViewCustomAdapter_forNonActivatedAccount;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Model.Fixer;
import com.keyfixer.partner.Model.Statistical;
import com.keyfixer.partner.Remote.IGoogleAPI;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.vivekkaushik.datepicker.DatePickerTimeline;
import com.vivekkaushik.datepicker.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class StatisticalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    private GoogleMap mMap;
    private IGoogleAPI mService;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
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
    Marker mCurrent;
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

    //datePicker
    DatePickerTimeline datePickerTimeline;

    //statistical listview
    ListView lvHistory;
    CustomListView_Statistical adapter;
    TextView txtTotalTripOfDay;
    TextView txtTotalFeeOfDay;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistical);
        //init view
        Initializing();
    }

    private void Initializing() {
        lvHistory = (ListView) findViewById(R.id.lst_request_of_day);
        txtTotalTripOfDay = (TextView) findViewById(R.id.txtTotal);
        txtTotalFeeOfDay = (TextView) findViewById(R.id.txt_Day_totalRequest);
        datePickerTimeline = (DatePickerTimeline) findViewById(R.id.datePickerTimeLine);
        datePickerTimeline.setInitialDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerTimeline.setDateTextColor(Color.WHITE);
        datePickerTimeline.setDayTextColor(Color.WHITE);
        datePickerTimeline.setMonthTextColor(Color.WHITE);
        datePickerTimeline.setOnDateSelectedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day, int dayOfWeek) {
                InitListByDate(year, month, dayOfWeek);
            }
        });

        slider = (RelativeLayout) findViewById(R.id.slider);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        slider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.addDrawerListener(toggle);
                toggle.syncState();
            }
        });
        //menu options
        Common.isAdmin = Common.currentFixer.isAdmin();
        if (Common.currentFixer.getJobFee() <= 0){
            Common.isActivated = false;
            Toast.makeText(this , "Tài khoản vừa hết tiền" , Toast.LENGTH_SHORT).show();
        }
        if (Common.isAdmin){
            toggleVisibility(navigationView.getMenu(), R.id.nav_provide_admin_rights, true);
            toggleVisibility(navigationView.getMenu(), R.id.nav_fixer_account_activate, true);
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
    }

    private void toggleVisibility(Menu menu, int id, boolean visible) {
        menu.findItem(id).setVisible(visible);
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
        getMenuInflater().inflate(R.menu.statistical , menu);
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
            Initializing();
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
            Intent home = new Intent(StatisticalActivity.this, FixerHome.class);
            startActivity(home);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //hàm quan trọng, xóa là chết m* :)))
    private void InitListByDate(int year, int month, int date){
        final double[] finalTotalFee = {0};
        final double[] totalRequest = {0};
        final List<Statistical> list = new ArrayList<>();
        DatabaseReference statisticalRef = FirebaseDatabase.getInstance().getReference(Common.statistical_tbl).child(Common.FixerID);
        statisticalRef.child(year + "").child(month + "").child(date + "").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                for (DataSnapshot item:snapshots){
                    Statistical statistical = item.getValue(Statistical.class);
                    finalTotalFee[0] += statistical.getTotalFee();
                    totalRequest[0] += 1;
                    list.add(statistical);
                }
                txtTotalFeeOfDay.setText("$" + finalTotalFee);
                txtTotalTripOfDay.setText("Đã hoàn tất " + totalRequest + " chuyến");
                adapter = new CustomListView_Statistical(getBaseContext(), R.layout.custom_listview_statistical, list);
                lvHistory.setAdapter(adapter);
                lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Statistical item = list.get(position);
                        ShowSpecificBill(item);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ShowSpecificBill(Statistical statistical){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StatisticalActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View service_bill = inflater.inflate(R.layout.specific_statistical_dialog , null);

        TextView txtTotal = (TextView) service_bill.findViewById(R.id.txtTotal);
        TextView txtCustomername = (TextView) service_bill.findViewById(R.id.txtName);
        TextView txtCustomerPhone = (TextView) service_bill.findViewById(R.id.customer_phone);
        TextView txtServiceName = (TextView) service_bill.findViewById(R.id.txtServiceName);
        TextView txtServiceFee = (TextView) service_bill.findViewById(R.id.txtServiceFee);
        TextView txtVATFee = (TextView) service_bill.findViewById(R.id.txt_ServiceVATFee);
        TextView txtFixLocation = (TextView) service_bill.findViewById(R.id.txtFix_Location);
        TextView txtFixTime = (TextView) service_bill.findViewById(R.id.txtFix_time);

        txtTotal.setText("$" + statistical.getTotalFee());
        txtCustomername.setText(statistical.getCustomerName());
        String phoneNumber = statistical.getCustomerPhone().substring(0,4) + " " + statistical.getCustomerPhone().substring(5,7) + " " + statistical.getCustomerPhone().substring(8);
        txtCustomerPhone.setText(phoneNumber);
        txtServiceName.setText(statistical.getServiceName());
        txtServiceFee.setText("$" + statistical.getServiceFee());
        txtVATFee.setText("$" + statistical.getVatFee());
        txtFixLocation.setText(statistical.getFixLocation());
        txtFixTime.setText(statistical.getCompletedHour() + " giờ " +
                            statistical.getCompletedMinutes() + " phút, ngày " +
                            statistical.getCompletedMonthDate() + " tháng " +
                            statistical.getCompletedMonth() + " năm " +
                            statistical.getCompletedYear());
        alertDialog.setView(service_bill);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void ShowNotActivatedListDialog(List<Fixer> list) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StatisticalActivity.this);
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
                    Fixer fixer = getNonActivatedList().get(position);
                    ActiveAccount(fixer);
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

    private void ShowActivatedListDialog(List<Fixer> list) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StatisticalActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View nonactivated_account = inflater.inflate(R.layout.layout_fixerlist_waiting_for_check , null);

        final CircleImageView avatar = (CircleImageView) nonactivated_account.findViewById(R.id.personal_avatar_);
        final TextView personal_name = (TextView) nonactivated_account.findViewById(R.id.personal_name_);
        final TextView personal_email = (TextView) nonactivated_account.findViewById(R.id.personal_email_);
        final ProgressBar progressBar = (ProgressBar) nonactivated_account.findViewById(R.id.progressBar_);
        final ListView listView_nonActivatedAccount = (ListView) nonactivated_account.findViewById(R.id.activated_account_list);
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
                    Fixer fixer = getNonActivatedList().get(position);
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
                        if (fixer.isActivated())
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

    private void ActiveAccount(final Fixer item){
        android.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new android.app.AlertDialog.Builder(this , android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new android.app.AlertDialog.Builder(this);

        builder.setMessage("Kích hoạt tài khoản cho thợ " + item.getStrName()).setPositiveButton("Kích hoạt" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog , int which) {
                dialog.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog(StatisticalActivity.this);
                waitingDialog.show();

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        boolean isActive = true;

                        Map<String, Object> updateinfo = new HashMap<>();
                        updateinfo.put("activated" , isActive);

                        DatabaseReference fixerInformation = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
                        fixerInformation.child(account.getId())
                                .updateChildren(updateinfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            ActiveSuccess();
                                        else
                                            ActiveFailed();
                                        waitingDialog.dismiss();
                                    }
                                });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });
                finish();
            }
        }).setNegativeButton("Hủy" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog , int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void AddAdminRuleAccount(final Fixer item) {
        android.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new android.app.AlertDialog.Builder(this , android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new android.app.AlertDialog.Builder(this);

        builder.setMessage("Thêm quyền admin tài khoản  " + item.getStrName()).setPositiveButton("Thêm" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog , int which) {
                dialog.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog(StatisticalActivity.this);
                waitingDialog.show();

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {

                        Map<String, Object> updateinfo = new HashMap<>();
                        updateinfo.put("admin" , true);

                        DatabaseReference fixerInformation = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
                        fixerInformation.child(account.getId())
                                .updateChildren(updateinfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            AddAdminRuleSuccess();
                                        else
                                            AddAdminRuleFailed();
                                        waitingDialog.dismiss();
                                    }
                                });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });
                finish();
            }
        }).setNegativeButton("Hủy" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog , int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void ShowDialogupdateServiceType() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StatisticalActivity.this);
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
                final android.app.AlertDialog waitingDialog = new SpotsDialog(StatisticalActivity.this);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StatisticalActivity.this);
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
                final android.app.AlertDialog waitingDialog = new SpotsDialog(StatisticalActivity.this);
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
                Intent intent = new Intent(StatisticalActivity.this , MainActivity.class);
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

    private void AlertUser(){
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Vui lòng tái kích hoạt GPS trên thiết bị để cập nhật lại dịch vụ");
        dialog.setCancelable(true);
        dialog.show();
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
                                Toast.makeText(StatisticalActivity.this , "Đang tải ... " , Toast.LENGTH_SHORT).show();
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

                                                            Toast.makeText(StatisticalActivity.this , "Tải hoàn tất" , Toast.LENGTH_SHORT).show();
                                                        else
                                                            Toast.makeText(StatisticalActivity.this , "Tải thất bại" , Toast.LENGTH_SHORT).show();
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

}