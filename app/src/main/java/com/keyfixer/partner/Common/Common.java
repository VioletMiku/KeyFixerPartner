package com.keyfixer.partner.Common;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.keyfixer.partner.Model.Fixer;
import com.keyfixer.partner.Model.Rate;
import com.keyfixer.partner.Remote.FCMClient;
import com.keyfixer.partner.Remote.IFCMService;
import com.keyfixer.partner.Remote.IGoogleAPI;
import com.keyfixer.partner.Remote.RetrofitClient;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Common {

    public static DatabaseReference onlineref, currentUserref;
    public static boolean isAdmin;
    public static boolean isActivated;
    public static String used_service = "";
    public static String used_service1 = "";
    public static String used_service2 = "";
    public static String service_chose;
    public static final String fixer_tbl = "Fixers";
    public static final String fixer_inf_tbl = "Users";
    public static final String customer_tbl = "Customers";
    public static final String fix_request_tbl = "FixRequest";
    public static final String rate_detail_tbl = "RateDetails";
    public static final String token_tbl = "Tokens";
    public static final int PICK_IMAGE_REQUEST = 9999;
    public static final String statistical_tbl = "StatisticalActivity";
    public static double house_lock_service = 30000;
    public static double car_lock_service = 25000;
    public static double motorbyke_lock_service = 20000;
    public static String FixerID = "";
    public static String CustomerID = "";

    public static Fixer currentFixer;
    public static Location mLastLocation = null;

    public static final String baseUrl = "https://maps.googleapis.com";
    public static final String fcmUrl = "https://fcm.googleapis.com/";

    static DatabaseReference rateDetailRef = FirebaseDatabase.getInstance().getReference(Common.rate_detail_tbl);
    static DatabaseReference fixerInformationRef = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
    static double ratingStars = 0.0;

    public static void DecreaseRate(final String fixerID){
        Rate rate = new Rate();
        rate.setRate(String.valueOf(ratingStars));
        rate.setComment("TỰ HỦY");

        rateDetailRef.child(fixerID)
                .push().setValue(rate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                rateDetailRef.child(fixerID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        double averageStars = 2.0;
                        int count = 0;
                        for (DataSnapshot postsnapshot : dataSnapshot.getChildren()){
                            Rate rate = postsnapshot.getValue(Rate.class);
                            averageStars += Double.parseDouble(rate.getRate());
                            count ++;
                        }
                        double finalAverage = averageStars / count;
                        DecimalFormat decimalFormat = new DecimalFormat("#.#");
                        final String valueUpdate = decimalFormat.format(finalAverage);

                        Map<String, Object> fixerUpdateRate = new HashMap<>();
                        fixerUpdateRate.put("rates", valueUpdate);

                        fixerInformationRef.child(fixerID).updateChildren(fixerUpdateRate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Common.currentFixer.setRates(valueUpdate);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public static double formulaPrice(double km, double service_fee){
        double fee = 0;
        fee += service_fee;

        if (km < 2){
            return fee;
        } else {
            return fee + (km*4000);
        }
    }

    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseUrl).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmUrl).create(IFCMService.class);
    }

    public static void RemoveRequest(){
        DatabaseReference request_tbl = FirebaseDatabase.getInstance().getReference(Common.fix_request_tbl);
        request_tbl.child(Common.FixerID).removeValue();
    }
}
