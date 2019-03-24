package com.keyfixer.partner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class PlashScreen extends AppCompatActivity {

    LinearLayout l1;
    Animation uptodown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plash_screen);

        l1 = (LinearLayout) findViewById(R.id.number11);
        uptodown = AnimationUtils.loadAnimation(this, R.anim.uptodown);
        l1.setAnimation(uptodown);

        Thread callHomeThread = new Thread(){
            @Override
            public void run(){
                try{
                    sleep(1500);
                    Intent intent = new Intent(PlashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        callHomeThread.start();
    }
}
