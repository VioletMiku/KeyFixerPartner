package com.keyfixer.partner;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.github.badoualy.datepicker.DatePickerTimeline;
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
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.keyfixer.partner.Adapter.CustomListView_Statistical;
import com.keyfixer.partner.Adapter.ListViewCustomAdapter_forNonActivatedAccount;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Model.Fixer;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class StatisticalManagementActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MaterialSpinner.OnItemSelectedListener, View.OnClickListener {

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
    MaterialSpinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistical_management);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        slider = (RelativeLayout) findViewById(R.id.slider1);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout3);
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
            Toast.makeText(StatisticalManagementActivity.this , "Tài khoản vừa hết tiền" , Toast.LENGTH_SHORT).show();
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
            Picasso.with(StatisticalManagementActivity.this).load(Common.currentFixer.getAvatarUrl()).into(imageAvatar);
        }
        spinner = (MaterialSpinner) findViewById(R.id.spinner1);
        spinner.setItems("Thống kê hàng ngày", "Thống kê theo quý");
        spinner.setOnItemSelectedListener(this);
        loadFragment(new DailyStatisticalManagementFragment());
    }

    @Override
    public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
        Fragment fragment = null;
        if (item == "Thống kê hàng ngày"){
            fragment = new DailyStatisticalManagementFragment();
        } else{
            fragment = new MonthlyStatisticalManagement();
        }
        loadFragment(fragment);
    }

    private void loadFragment (Fragment fragment){
        if (fragment != null)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment1, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_fix_history:
                Intent intent = new Intent(StatisticalManagementActivity.this, Main2Activity.class);
                startActivity(intent);
                break;
            case R.id.nav_statistical_management:
                Intent intent2 = new Intent(StatisticalManagementActivity.this, StatisticalManagementActivity.class);
                startActivity(intent2);
                break;
            case R.id.nav_servicetype:
                ShowDialogupdateServiceType();
                break;
            case R.id.nav_signout:
                Signout();
                break;
            case R.id.nav_update_information:
                ShowDialogUpdateInfo();
                break;
            case R.id.nav_fixer_account_activate:
                getNonActivatedList();
                break;
            case R.id.nav_provide_admin_rights:
                getActivatedList();
                break;
            case R.id.nav_fix_home:
                Intent home = new Intent(StatisticalManagementActivity.this, FixerHome.class);
                startActivity(home);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout3);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private void toggleVisibility(Menu menu, int id, boolean visible) {
        menu.findItem(id).setVisible(visible);
    }

    private void ShowNotActivatedListDialog(final List<Fixer> list) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(StatisticalManagementActivity.this);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StatisticalManagementActivity.this);
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
                Picasso.with(StatisticalManagementActivity.this).load(Common.currentFixer.getAvatarUrl()).into(avatar);
            }
            personal_name.setText(Common.currentFixer.getStrName());
            personal_email.setText(Common.currentFixer.getStrEmail());
        }

        if (list != null){
            adapter = new ListViewCustomAdapter_forNonActivatedAccount(StatisticalManagementActivity.this, R.layout.layout_custom_listview_nonactivated_account, list);
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
                                            new ListViewCustomAdapter_forNonActivatedAccount(StatisticalManagementActivity.this, R.layout.layout_custom_listview_nonactivated_account, fixerList);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StatisticalManagementActivity.this);
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
                final android.app.AlertDialog waitingDialog = new SpotsDialog(StatisticalManagementActivity.this);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StatisticalManagementActivity.this);
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
                final android.app.AlertDialog waitingDialog = new SpotsDialog(StatisticalManagementActivity.this);
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
        SweetAlertDialog dialog = new SweetAlertDialog(StatisticalManagementActivity.this, SweetAlertDialog.SUCCESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Kích hoạt thành công!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void ActiveFailed(){
        SweetAlertDialog dialog = new SweetAlertDialog(StatisticalManagementActivity.this, SweetAlertDialog.ERROR_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Kích hoạt thất bại!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void AddAdminRuleSuccess(){
        SweetAlertDialog dialog = new SweetAlertDialog(StatisticalManagementActivity.this, SweetAlertDialog.SUCCESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Thêm quyền thành công!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void AddAdminRuleFailed(){
        SweetAlertDialog dialog = new SweetAlertDialog(StatisticalManagementActivity.this, SweetAlertDialog.ERROR_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Không thể thêm quyền!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void UpdateSuccess(){
        SweetAlertDialog dialog = new SweetAlertDialog(StatisticalManagementActivity.this, SweetAlertDialog.SUCCESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Cập nhật thành công!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void UpdateFailed(){
        SweetAlertDialog dialog = new SweetAlertDialog(StatisticalManagementActivity.this, SweetAlertDialog.ERROR_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Cập nhật thất bại!");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void Signout() {
        android.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new android.app.AlertDialog.Builder(StatisticalManagementActivity.this , android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new android.app.AlertDialog.Builder(StatisticalManagementActivity.this);

        builder.setMessage("Thật sự muốn thoát!?").setPositiveButton(android.R.string.ok , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog , int which) {
                AccountKit.logOut();
                Intent intent = new Intent(StatisticalManagementActivity.this , MainActivity.class);
                startActivity(intent);
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
        SweetAlertDialog dialog = new SweetAlertDialog(StatisticalManagementActivity.this, SweetAlertDialog.WARNING_TYPE);
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

                                                            Toast.makeText(StatisticalManagementActivity.this , "Tải hoàn tất" , Toast.LENGTH_SHORT).show();
                                                        else
                                                            Toast.makeText(StatisticalManagementActivity.this , "Tải thất bại" , Toast.LENGTH_SHORT).show();
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
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            loadFragment(new DailyStatisticalManagementFragment());
        }
        return true;
    }
}
