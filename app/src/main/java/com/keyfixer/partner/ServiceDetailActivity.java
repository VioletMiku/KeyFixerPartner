package com.keyfixer.partner;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Model.DetailFee;
import com.keyfixer.partner.Model.Service;

import java.util.Calendar;


public class ServiceDetailActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    TextView txtDate, txtTotalFee, txtDistance, txtDistanceFee, txtStartAddress, txtEndAddress, txtVATFee, txtServiceName, txtServiceFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Initializing();
    }

    void Initializing(){
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtTotalFee = (TextView) findViewById(R.id.txtFee);
        txtDistance = (TextView) findViewById(R.id.txtDistance);
        txtDistanceFee = (TextView) findViewById(R.id.txtdistanceFee);
        txtStartAddress = (TextView) findViewById(R.id.txt_startAddress);
        txtEndAddress = (TextView) findViewById(R.id.txt_endAddress);
        txtVATFee = (TextView) findViewById(R.id.txtVAT);
        txtServiceName = (TextView) findViewById(R.id.service_name);
        txtServiceFee = (TextView) findViewById(R.id.service_fee);
    }

    DetailFee getData(){
        DetailFee detailFee = new DetailFee();
        detailFee.setDistance(getIntent().getStringExtra("distance"));
        detailFee.setEndAddress(getIntent().getStringExtra("end_address"));
        detailFee.setStartAddress(getIntent().getStringExtra("start_address"));
        detailFee.setTotalFee(getIntent().getDoubleExtra("total", 0.0));
        detailFee.setServices(getIntent().<Service>getParcelableArrayListExtra("services"));
        return detailFee;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        SettingInformation();
    }

    private void SettingInformation() {
        double totalfee = getData().getTotalFee();
        if (getIntent() != null){
            Calendar calendar = Calendar.getInstance();
            String date = String.format("%s, %d/%d", convertToDayofWeek(calendar.get(Calendar.DAY_OF_WEEK)), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH));
            txtDate.setText(date);
            txtVATFee.setText("" + totalfee*0.4);
            txtDistance.setText(getData().getDistance() + " meter(s)");
            txtEndAddress.setText(getData().getEndAddress());
            txtStartAddress.setText(getData().getStartAddress());
            txtDistanceFee.setText("" + Double.parseDouble(getData().getDistance())*5);
            txtTotalFee.setText(totalfee + (totalfee*0.4) + "");
            if (Common.used_service == "S ử a   k h ó a   x e   g ắ n   m á y"){
                txtServiceName.setText("Sửa khóa xe gắn máy/2 bánh");
                txtServiceFee.setText(Common.motorbyke_lock_service + " vnd");
            } else if (Common.used_service == "S ử a   k h ó a   n h à"){
                txtServiceName.setText("Sửa khóa nhà");
                txtServiceFee.setText(Common.house_lock_service + " vnd");
            } else if (Common.used_service == "S ử a   k h ó a   x e   h ơ i"){
                txtServiceName.setText("Sửa khóa xe hơi");
                txtServiceFee.setText(Common.car_lock_service + " vnd");
            }
            String[] location_end = getIntent().getStringExtra("location_end").split(",");
            Log.e("data","" + location_end.toString());
            LatLng fixed = new LatLng(Double.parseDouble(location_end[0]), Double.parseDouble(location_end[1]));

            mMap.addMarker(new MarkerOptions().position(fixed).title("Đã sửa xong ở đây").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fixed, 12.0f));
        }
    }

    private String convertToDayofWeek(int day) {
        switch (day){
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            case Calendar.SUNDAY:
                return "Sunday";
                default:
                    return "Cyka blyat! That's a doom day, sucker capitalism western! ";
        }
    }
}
