package com.keyfixer.partner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    RelativeLayout welcomeLayout;
    Button btnSignIn, btnRegister;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    TextView txt_forget_password;

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

        //Init Firebase
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.fixer_inf_tbl);
        txt_forget_password = (TextView) findViewById(R.id.txt_forgot_password);
        //Init view
        GetButtonControl();
    }

    void GetButtonControl(){
        btnSignIn = (Button) findViewById(R.id.btn_signin);
        btnRegister = (Button) findViewById(R.id.btn_register);
        welcomeLayout = (RelativeLayout) findViewById(R.id.welcome_Layout);
        btnSignIn.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        txt_forget_password.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_signin:
                ShowLoginDialog();
                break;
            case R.id.btn_register:
                ShowRegisterDialog();
                break;
        }
    }

    private void ShowRegisterDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Đăng kí tài khoản ");
        dialog.setMessage("Làm ơn dùng email mà đăng ký giùm mình nhé ! ");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register, null);

        final MaterialEditText edtEmail = register_layout.findViewById(R.id.edt_Email);
        final MaterialEditText edtPass = register_layout.findViewById(R.id.edt_Password);
        final MaterialEditText edtName = register_layout.findViewById(R.id.edt_Name);
        final MaterialEditText edtPhone = register_layout.findViewById(R.id.edt_Phone);

        dialog.setView(register_layout);

        //Set for the agree button :v ... ofcourse
        dialog.setPositiveButton("Đăng ký", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                //validation
                if (TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(welcomeLayout,"Làm ơn nhập email giùm",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPass.getText().toString())){
                    Snackbar.make(welcomeLayout,"Làm ơn nhập mật khẩu giùm",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtPass.getText().toString().length() < 6){
                    Snackbar.make(welcomeLayout,"Mật khẩu ngắn quá!!!",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPhone.getText().toString())){
                    Snackbar.make(welcomeLayout,"Làm ơn cung cấp số điện thoại nhé",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtName.getText().toString())){
                    Snackbar.make(welcomeLayout,"Thiếu cái tên nữa",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                //Done ... now, let register new user
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPass.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //save user to db
                        User user = new User();
                        user.setStrEmail(edtEmail.getText().toString());
                        user.setStrPassword(edtPass.getText().toString());
                        user.setStrName(edtName.getText().toString());
                        user.setStrPhone(edtPhone.getText().toString());

                        //use email as key
                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(welcomeLayout,"Đăng ký thành công!",Snackbar.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(welcomeLayout,"Đăng ký thất bại " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(welcomeLayout,"Việc đăng ký thất bại!" + e.getMessage(),Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void ShowLoginDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Đăng nhập");
        dialog.setMessage("Đăng nhập để sử dụng nhé ! ");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_login, null);

        final MaterialEditText edtEmail = register_layout.findViewById(R.id.edt_Email);
        final MaterialEditText edtPass = register_layout.findViewById(R.id.edt_Password);

        dialog.setView(register_layout);

        //Set for the agree button :v ... ofcourse
        dialog.setPositiveButton("Đăng nhập", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                //set disable button sign in if is processing
                btnSignIn.setEnabled(false);
                //validation
                if (TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(welcomeLayout,"Làm ơn nhập email giùm",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPass.getText().toString())){
                    Snackbar.make(welcomeLayout,"Làm ơn nhập mật khẩu giùm",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                final SpotsDialog waiting_dialog = new SpotsDialog(MainActivity.this);
                waiting_dialog.show();
                //Login
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waiting_dialog.dismiss();

                                FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl).child(FirebaseAuth
                                .getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Common.currentUser = dataSnapshot.getValue(User.class);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                startActivity(new Intent(MainActivity.this,FixerHome.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waiting_dialog.dismiss();
                        Snackbar.make(welcomeLayout,"Đăng nhập thất bại!",Snackbar.LENGTH_SHORT).show();
                        //set enable button sign in if it failed
                        btnSignIn.setEnabled(true);
                    }
                });
            }
        });
        dialog.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onTouch(View view , MotionEvent motionEvent) {
        switch (view.getId()){
            case R.id.txt_forgot_password:
                ShowDialogForgetPassword(view);
                return true;
        }
        return false;
    }

    private void ShowDialogForgetPassword(final View view) {
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(MainActivity.this);
        alertdialog.setTitle("Quên mật khẩu");
        alertdialog.setMessage("Hãy cung cấp địa chỉ email");
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View forgot_password_layout = inflater.inflate(R.layout.layout_forget_password, null);
        final MaterialEditText edtEmail = (MaterialEditText)forgot_password_layout.findViewById(R.id.edt_Email);
        alertdialog.setView(forgot_password_layout);
        alertdialog.setPositiveButton("Đặt lại mật khẩu" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface , int i) {
                final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();

                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(view, "Đường link thay đổi mật khẩu vừa được gửi. Vui lòng check mail", Snackbar.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(view, "Địa chỉ email không tồn tại .. Vui lòng kiểm tra và nhập lại!", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        alertdialog.setNegativeButton("Hủy" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface , int i) {
                dialogInterface.dismiss();
            }
        });
        alertdialog.show();
    }
}

