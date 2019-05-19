package com.keyfixer.partner;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DateSorter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.keyfixer.partner.Adapter.CustomGridView_statisticalManagement;
import com.keyfixer.partner.Adapter.CustomListView_Statistical;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Helper.ExpandableHeightGridView;
import com.keyfixer.partner.Model.Fixer;
import com.keyfixer.partner.Model.Statistical;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class DailyStatisticalManagementFragment extends Fragment {
    View view;
    Context context;
    ExpandableHeightGridView grid;
    ProgressBar progressBar;
    TextView txtTotalTripOfDay;
    TextView txtTotalFeeOfDay;
    TextView txtTotalRevenue;
    EditText txtSearchString;
    //datePicker
    DatePickerTimeline datePickerTimeline;

    //statistical listview
    ListView lvHistory;
    CustomListView_Statistical adapter;

    //personal card
    RelativeLayout personalCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.daily_statistical_management, null);
        this.context = container.getContext();
        Initializing(view);
        return view;
    }

    private void Initializing(View view) {
        InitTotalRevenue(view);
        InitPersonalCard(view);
        InitGridContents(view);
    }

    private void InitTotalRevenue(View view) {
        txtTotalRevenue = (TextView) view.findViewById(R.id.totalRevenue);
        final DatabaseReference statistical_tbl = FirebaseDatabase.getInstance().getReference(Common.statistical_tbl);
        statistical_tbl.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final double[] Revenue = {0};
                Iterable<DataSnapshot> fixers = dataSnapshot.getChildren();
                for (DataSnapshot fixer:fixers){
                    Iterable<DataSnapshot> years = fixer.getChildren();
                    for (DataSnapshot year:years){
                        Iterable<DataSnapshot> months = year.getChildren();
                        for (DataSnapshot month:months){
                            Iterable<DataSnapshot> dates = month.getChildren();
                            for (DataSnapshot date:dates){
                                Iterable<DataSnapshot> statisticals = date.getChildren();
                                for (DataSnapshot item:statisticals){
                                    Statistical statistical = item.getValue(Statistical.class);
                                    Revenue[0] += statistical.getTotalFee();
                                }
                            }
                        }
                    }
                }
                txtTotalRevenue.setText("$" + Revenue[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void InitPersonalCard(View view) {
        personalCard = (RelativeLayout) view.findViewById(R.id.personal_card);
        personalCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference fixer_tbl = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
                fixer_tbl.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        for (DataSnapshot item:snapshots){
                            Fixer fixer = item.getValue(Fixer.class);
                            if (fixer.getStrPhone().equals(Common.currentFixer.getStrPhone())){
                                InitDailyStatistical(item.getKey());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        grid = (ExpandableHeightGridView) view.findViewById(R.id.grid);
        grid.setExpanded(true);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        ImageView img = (ImageView) view.findViewById(R.id.admin_avatar);
        TextView txtfixerName = (TextView) view.findViewById(R.id.fixer_username);

        if (Common.currentFixer.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentFixer.getAvatarUrl()))
            Picasso.with(context).load(Common.currentFixer.getAvatarUrl()).into(img);
        txtfixerName.setText(Common.currentFixer.getStrName());
    }

    private void InitGridContents(final View view) {
        txtSearchString = (EditText) view.findViewById(R.id.searchBar);
        final List<Fixer> fixers = new ArrayList<>();
        DatabaseReference fixer_tbl = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
        fixer_tbl.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                for (DataSnapshot item:snapshots){
                    Fixer fixer = item.getValue(Fixer.class);
                    if (!fixer.getStrPhone().equals(Common.currentFixer.getStrPhone()) && !item.getKey().equals(Common.FixerID)){
                        fixers.add(fixer);
                    }
                }
                IntegrateWithGridView(fixers);
                txtSearchString.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        grid.setAdapter(null);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        grid.setAdapter(null);
                        final List<Fixer> searchedContents = new ArrayList<>();
                        for (Fixer item:fixers){
                            if (item.getStrName().contains(s.toString()))
                                searchedContents.add(item);
                        }
                        if (searchedContents != null){
                            progressBar.setVisibility(View.INVISIBLE);
                            CustomGridView_statisticalManagement adapter = new CustomGridView_statisticalManagement(context, R.layout.grid_single, searchedContents);
                            grid.setAdapter(adapter);
                            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Fixer fixer = searchedContents.get(position);
                                    GridView_onItemClick(fixer);
                                }
                            });
                        }
                        if (s.toString() == null || TextUtils.isEmpty(s.toString()))
                            IntegrateWithGridView(fixers);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void IntegrateWithGridView(final List<Fixer> fixers) {
        grid.setAdapter(null);
        if (fixers != null){
            progressBar.setVisibility(View.INVISIBLE);
            CustomGridView_statisticalManagement adapter = new CustomGridView_statisticalManagement(context, R.layout.grid_single, fixers);
            grid.setAdapter(adapter);
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Fixer fixer = fixers.get(position);
                    GridView_onItemClick(fixer);
                }
            });
        }
    }

    private void GridView_onItemClick(final Fixer model) {
        DatabaseReference fixer_tbl = FirebaseDatabase.getInstance().getReference(Common.fixer_inf_tbl);
        fixer_tbl.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                for (DataSnapshot item:snapshots){
                    Fixer fixer = item.getValue(Fixer.class);
                    if (fixer.getStrPhone().equals(model.getStrPhone())){
                        InitDailyStatistical(item.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void InitDailyStatistical(final String key) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = this.getLayoutInflater();
        View layout_update_info = inflater.inflate(R.layout.daily_statistical_customdialog , null);

        lvHistory = (ListView) layout_update_info.findViewById(R.id.lst_request_of_day);
        txtTotalTripOfDay = (TextView) layout_update_info.findViewById(R.id.txt_Day_totalRequest);
        txtTotalFeeOfDay = (TextView) layout_update_info.findViewById(R.id.txtTotal);
        datePickerTimeline = (DatePickerTimeline) layout_update_info.findViewById(R.id.datePicker);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        InitListByDate(year, month, date, key);
        datePickerTimeline.setOnDateSelectedListener(new DatePickerTimeline.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day, int dayOfWeek) {
                InitListByDate(year, month, day, key);
            }
        });
        alertDialog.setView(layout_update_info);
        alertDialog.setNegativeButton("Đóng" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface , int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();

    }

    //hàm quan trọng, xóa là chết m* :)))
    private void InitListByDate(int year, int month, int date, String key){
        final double[] finalTotalFee = {0};
        final double[] totalRequest = {0};
        final List<Statistical> list = new ArrayList<>();
        final DatabaseReference statisticalRef = FirebaseDatabase.getInstance().getReference(Common.statistical_tbl).child(key);
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
}
