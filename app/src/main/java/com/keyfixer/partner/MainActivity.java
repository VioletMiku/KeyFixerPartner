package com.keyfixer.partner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.EventLogTags;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Model.Fixer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    RelativeLayout welcomeLayout;
    Button btnContinue;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    TextView txt_forget_password;
    private static final int REQUEST_CODE = 1000;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //before set content view
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_main);
        printkeyhash();
        //Init paper
        Paper.init(this);
        //Init Firebase
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.fixer_inf_tbl);
        txt_forget_password = (TextView) findViewById(R.id.txt_forgot_password);
        //Init view
        GetButtonControl();
        CheckAccountFee();
        ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null){
            createInternetNotAvailableDialog();
        }else{
            // if network is available
            //Auto login with Facebook account kit for second time
            if (AccountKit.getCurrentAccessToken() != null) {
                //create dialog
                final AlertDialog waitingDialog = new SpotsDialog(this);
                waitingDialog.show();
                waitingDialog.setMessage("Chờ trong giây lát");
                waitingDialog.setCancelable(false);
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {

                        users.child(account.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Common.currentFixer = dataSnapshot.getValue(Fixer.class);
                                Common.isAdmin = dataSnapshot.getValue(Fixer.class).isAdmin();
                                Common.isActivated = dataSnapshot.getValue(Fixer.class).isActivated();
                                Intent homeIntent = new Intent(MainActivity.this, FixerHome.class);
                                startActivity(homeIntent);

                                //dismiss dialog
                                waitingDialog.dismiss();
                                finish();
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
        }

    }

    private void createInternetNotAvailableDialog(){
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Vui lòng kiểm tra lại kết nối mạng");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void printkeyhash() {
        try{
            PackageInfo info = getPackageManager().getPackageInfo("com.keyfixer.partner", PackageManager.GET_SIGNATURES);
            for (Signature signature:info.signatures){
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KEYHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void CheckAccountFee(){
        final Map<String, Object> updateinfo = new HashMap<>();
        updateinfo.put("activated" , false);
        final Map<String, Object> updateinfo2 = new HashMap<>();
        updateinfo.put("activated" , true);
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                final DatabaseReference fixer_tbl = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
                fixer_tbl.child(account.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Fixer fixer = dataSnapshot.getValue(Fixer.class);
                        if (fixer.getJobFee() == 0){
                            fixer_tbl.child(account.getId()).updateChildren(updateinfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Common.isOver = true;
                                }
                            });
                        } else{
                            fixer_tbl.child(account.getId()).updateChildren(updateinfo2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
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

    void GetButtonControl(){
        //btnSignIn = (Button) findViewById(R.id.btn_signin);
        btnContinue = (Button) findViewById(R.id.btn_Continue);
        welcomeLayout = (RelativeLayout) findViewById(R.id.welcome_Layout);
        btnContinue.setOnClickListener(this);
        //txt_forget_password.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_Continue:
                ConnectivityManager conMgr =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
                if (netInfo == null){
                    createInternetNotAvailableDialog();
                }else {
                    SignInWithPhone();
                }
                break;
        }
    }

    private void SignInWithPhone() {
        Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder
                = new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent data) {
        super.onActivityResult(requestCode , resultCode , data);
        if (requestCode == REQUEST_CODE){
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (result.getError() != null){
                Toast.makeText(this , "Có lỗi xảy ra trong quá trình đăng nhập!" , Toast.LENGTH_SHORT).show();
                Log.e("ERROR_while_login", "" + result.getError().getErrorType().getMessage());
                return;
            } else if (result.wasCancelled()){
                Toast.makeText(this , "Đã hủy" , Toast.LENGTH_SHORT).show();
                return;
            } else{
                if (result.getAccessToken() != null){
                    final AlertDialog waitingDialog = new SpotsDialog(this);
                    waitingDialog.show();
                    waitingDialog.setMessage("Chờ trong giây lát");
                    waitingDialog.setCancelable(false);
                    //Get current phone
                    AccountKit.getCurrentAccount((new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(final Account account) {
                            final String userId = account.getId();
                            users.orderByKey().equalTo(account.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.child(account.getId()).exists()) {// if not exist
                                        //create new fixer and login
                                        Fixer fixer = new Fixer();
                                        fixer.setStrPhone(account.getPhoneNumber().toString());
                                        fixer.setStrName(account.getPhoneNumber().toString());
                                        fixer.setAvatarUrl("");
                                        fixer.setRates("0.0");
                                        fixer.setCanFixHouseKey(true);//default service :))
                                        fixer.setCanFixCarKey(true);
                                        fixer.setCanFixBikeKey(false);
                                        fixer.setActivated(true);
                                        fixer.setAdmin(true);
                                        fixer.setJobFee(500000);
                                        //register to firebase
                                        users.child(account.getId()).setValue(fixer).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //login
                                                users.child(account.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Common.currentFixer = dataSnapshot.getValue(Fixer.class);
                                                        Common.isAdmin = dataSnapshot.getValue(Fixer.class).isAdmin();
                                                        Common.isActivated = dataSnapshot.getValue(Fixer.class).isActivated();
                                                        Intent homeIntent = new Intent(MainActivity.this, FixerHome.class);
                                                        Toast.makeText(MainActivity.this , "Nhớ sửa lại tên sau khi vào trang chính" , Toast.LENGTH_LONG).show();
                                                        startActivity(homeIntent);

                                                        //dismiss dialog
                                                        waitingDialog.dismiss();
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this , "Có lỗi xảy ra trong quá trình đăng kí thông tin" , Toast.LENGTH_SHORT).show();
                                                Log.e("ERROR_WHILE_REGISTER", "" + e.getMessage());
                                            }
                                        });
                                    } else{ // if user existing -> just login
                                        //login
                                        users.child(account.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Common.currentFixer = dataSnapshot.getValue(Fixer.class);
                                                Common.isAdmin = dataSnapshot.getValue(Fixer.class).isAdmin();
                                                Common.isActivated = dataSnapshot.getValue(Fixer.class).isActivated();
                                                Intent homeIntent = new Intent(MainActivity.this, FixerHome.class);
                                                startActivity(homeIntent);

                                                //dismiss dialog
                                                waitingDialog.dismiss();
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(MainActivity.this , "Đã có lỗi xảy ra trong quá trình liên kết với máy chủ" , Toast.LENGTH_SHORT).show();
                            Log.e("Error_linking_FB","" + accountKitError.getErrorType().getMessage());
                        }
                    }));
                }
            }
        }
    }
}

