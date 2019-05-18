package com.keyfixer.partner;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.badoualy.datepicker.DatePickerTimeline;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.keyfixer.partner.Adapter.CustomListView_Statistical;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Model.Statistical;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DailyStatisticalFragment extends Fragment{

    //datePicker
    DatePickerTimeline datePickerTimeline;

    //statistical listview
    ListView lvHistory;
    CustomListView_Statistical adapter;
    TextView txtTotalTripOfDay;
    TextView txtTotalFeeOfDay;
    Context context;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_daily_statistical, null);
        this.context = container.getContext();
        Initializing(view);
        return view;
    }

    private void Initializing(View view) {
        lvHistory = (ListView) view.findViewById(R.id.lst_request_of_day);
        txtTotalTripOfDay = (TextView) view.findViewById(R.id.txt_Day_totalRequest);
        txtTotalFeeOfDay = (TextView) view.findViewById(R.id.txtTotal);
        datePickerTimeline = (DatePickerTimeline) view.findViewById(R.id.datePicker);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        InitListByDate(year, month, date);
        datePickerTimeline.setOnDateSelectedListener(new DatePickerTimeline.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day, int dayOfWeek) {
                InitListByDate(year, month, day);
            }
        });
    }

    //hàm quan trọng, xóa là chết m* :)))
    private void InitListByDate(int year, int month, int date){
        final double[] finalTotalFee = {0};
        final double[] totalRequest = {0};
        final List<Statistical> list = new ArrayList<>();
        final DatabaseReference statisticalRef = FirebaseDatabase.getInstance().getReference(Common.statistical_tbl).child(Common.FixerID);
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
                txtTotalFeeOfDay.setText("$" + finalTotalFee[0]);
                txtTotalTripOfDay.setText("Đã hoàn tất " + (int) totalRequest[0] + " chuyến");
                adapter = new CustomListView_Statistical(context, R.layout.custom_listview_statistical, list);
                lvHistory.setAdapter(adapter);
                lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Statistical item = list.get(position);
                        Toast.makeText(context, "Đang fix", Toast.LENGTH_SHORT).show();
                        //ShowSpecificBill(item);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ShowSpecificBill(Statistical statistical){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        View service_bill = getLayoutInflater().inflate(R.layout.specific_statistical_dialog , null);
        TextView txtDetailTotal = (TextView) service_bill.findViewById(R.id.txtDetailTotal);
        TextView txtDetailCustomername = (TextView) service_bill.findViewById(R.id.txtDetailName);
        TextView txtDetailCustomerPhone = (TextView) service_bill.findViewById(R.id.customer_phone);
        TextView txtDetailServiceName = (TextView) service_bill.findViewById(R.id.txtServiceName);
        TextView txtDetailServiceFee = (TextView) service_bill.findViewById(R.id.txtServiceFee);
        TextView txtDetailVATFee = (TextView) service_bill.findViewById(R.id.txt_ServiceVATFee);
        TextView txtDetailFixLocation = (TextView) service_bill.findViewById(R.id.txtFix_Location);
        TextView txtDetailFixTime = (TextView) service_bill.findViewById(R.id.txtFix_time);

        txtDetailTotal.setText("$" + statistical.getTotalFee());
        txtDetailCustomername.setText(statistical.getCustomerName());
        String phoneNumber = statistical.getCustomerPhone().substring(0,4) + " " + statistical.getCustomerPhone().substring(5,7) + " " + statistical.getCustomerPhone().substring(8);
        txtDetailCustomerPhone.setText(phoneNumber);
        txtDetailServiceName.setText(statistical.getServiceName());
        txtDetailServiceFee.setText("$" + statistical.getServiceFee());
        txtDetailVATFee.setText("$" + statistical.getVatFee());
        txtDetailFixLocation.setText(statistical.getFixLocation());
        txtDetailFixTime.setText(statistical.getCompletedHour() + " giờ " +
                statistical.getCompletedMinutes() + " phút, ngày " +
                statistical.getCompletedMonthDate() + " tháng " +
                statistical.getCompletedMonth() + " năm " +
                statistical.getCompletedYear());
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setView(service_bill);
        AlertDialog dialog = alertDialog.create();
        dialog.show();
    }


}
