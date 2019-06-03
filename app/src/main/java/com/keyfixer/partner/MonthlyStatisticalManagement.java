package com.keyfixer.partner;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.badoualy.datepicker.DatePickerTimeline;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.keyfixer.partner.Adapter.CustomGridView_statisticalManagement;
import com.keyfixer.partner.Adapter.CustomListViewAdapter_forEachDayStatistical;
import com.keyfixer.partner.Adapter.CustomListView_Statistical;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Helper.ExpandableHeightGridView;
import com.keyfixer.partner.Model.DailyStatistical;
import com.keyfixer.partner.Model.Fixer;
import com.keyfixer.partner.Model.Statistical;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthlyStatisticalManagement extends Fragment implements OnChartValueSelectedListener {
    View view, dialogView;
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


    protected Typeface tfRegular;
    protected Typeface tfLight;
    PieChart totalChart;
    MaterialSpinner spinner;
    RadioButton quy1, quy2, quy3, quy4;
    private Typeface tf;
    TextView month1, month2, month3, TongDoanhThuTrongQuy;
    ListView lvMonth1, lvMonth2, lvMonth3;
    String YEAR;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.monthly_statistical_management, null);
        this.context = container.getContext();
        Initializing(view);
        tfRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-Regular.ttf");
        tfLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-Light.ttf");
        return view;
    }

    private void Initializing(View view) {
        InitTotalRevenue(view);
        InitPersonalCard(view);
        InitGridContents(view);
    }

    private void InitTotalRevenue(View view) {
        txtTotalRevenue = (TextView) view.findViewById(R.id.totalRevenue1);
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
        personalCard = (RelativeLayout) view.findViewById(R.id.personal_card1);
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
                                InitMonthlyStatistical(item.getKey());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        grid = (ExpandableHeightGridView) view.findViewById(R.id.grid1);
        grid.setExpanded(true);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
        ImageView img = (ImageView) view.findViewById(R.id.admin_avatar1);
        TextView txtfixerName = (TextView) view.findViewById(R.id.fixer_username1);

        if (Common.currentFixer.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentFixer.getAvatarUrl()))
            Picasso.with(context).load(Common.currentFixer.getAvatarUrl()).into(img);
        txtfixerName.setText(Common.currentFixer.getStrName());
    }

    private void InitGridContents(final View view) {
        txtSearchString = (EditText) view.findViewById(R.id.searchBar1_);
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
                        InitMonthlyStatistical(item.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void InitMonthlyStatistical(final String key) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = this.getLayoutInflater();
        View layout_update_info = inflater.inflate(R.layout.monthly_statistical_management_dialog , null);
        dialogView = layout_update_info;
        TongDoanhThuTrongQuy = (TextView) layout_update_info.findViewById(R.id.TongDoanhThuCuaQuy1);
        month1 = (TextView) layout_update_info.findViewById(R.id.Month1_);
        month2 = (TextView) layout_update_info.findViewById(R.id.Month2_);
        month3 = (TextView) layout_update_info.findViewById(R.id.Month3_);
        lvMonth1 = (ListView) layout_update_info.findViewById(R.id.FirstMonth1);
        lvMonth2 = (ListView) layout_update_info.findViewById(R.id.SecondMonth1);
        lvMonth3 = (ListView) layout_update_info.findViewById(R.id.ThirdMonth1);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int date = Calendar.getInstance().get(Calendar.DATE);
        PrepareSpinner(layout_update_info ,key);
        int index = spinner.getSelectedIndex();
        final String CURRENT_YEAR = spinner.getItems().get(index).toString();
        GenerateDataFromCloud("Quy2", CURRENT_YEAR, key);
        quy1 = (RadioButton) layout_update_info.findViewById(R.id.quy1_);
        quy2 = (RadioButton) layout_update_info.findViewById(R.id.quy2_);
        quy3 = (RadioButton) layout_update_info.findViewById(R.id.quy3_);
        quy4 = (RadioButton) layout_update_info.findViewById(R.id.quy4_);

        if (quy1.isChecked()) {
            GenerateDataFromCloud("Quy1", CURRENT_YEAR, key);
        }
        if (quy2.isChecked()) {
            GenerateDataFromCloud("Quy2", CURRENT_YEAR, key);
        }
        if (quy3.isChecked()) {
            GenerateDataFromCloud("Quy3", CURRENT_YEAR, key);
        }
        if (quy4.isChecked()) {
            GenerateDataFromCloud("Quy4", CURRENT_YEAR, key);
        }

        quy1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateDataFromCloud("Quy1", CURRENT_YEAR, key);
                quy1.setChecked(true);
            }
        });
        quy2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateDataFromCloud("Quy2", CURRENT_YEAR, key);
                quy2.setChecked(true);
            }
        });
        quy3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateDataFromCloud("Quy3", CURRENT_YEAR, key);
                quy3.setChecked(true);
            }
        });
        quy4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateDataFromCloud("Quy4", CURRENT_YEAR, key);
                quy4.setChecked(true);
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

    private void PrepareSpinner(View view, final String key){
        spinner = (MaterialSpinner) view.findViewById(R.id.Yearspinner1);
        List<String> yearList = new ArrayList<>();
        yearList.add("2019");
        yearList.add("2020");
        yearList.add("2021");
        spinner.setItems(yearList);
        String year = Calendar.getInstance().get(Calendar.YEAR) + "";
        if (Integer.parseInt(year) > FindMaxItem(yearList)){
            yearList.add(year);
            spinner.setItems(yearList);
        }
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                YEAR = view.getText().toString();
                GenerateDataFromCloud("Quy1", YEAR, key);
                if (quy1.isChecked()) {
                    GenerateDataFromCloud("Quy1", YEAR, key);
                }
                if (quy2.isChecked()) {
                    GenerateDataFromCloud("Quy2", YEAR, key);
                }
                if (quy3.isChecked()) {
                    GenerateDataFromCloud("Quy3", YEAR, key);
                }
                if (quy4.isChecked()) {
                    GenerateDataFromCloud("Quy4", YEAR, key);
                }

                quy1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GenerateDataFromCloud("Quy1", YEAR, key);
                        quy1.setChecked(true);
                    }
                });
                quy2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GenerateDataFromCloud("Quy2", YEAR, key);
                        quy2.setChecked(true);
                    }
                });
                quy3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GenerateDataFromCloud("Quy3", YEAR, key);
                        quy3.setChecked(true);
                    }
                });
                quy4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GenerateDataFromCloud("Quy4", YEAR, key);
                        quy4.setChecked(true);
                    }
                });
            }
        });
    }

    private int FindMaxItem(List<String> list){
        int tam;
        try{
            if (list != null){
                tam = Integer.parseInt(list.get(0));
                for (String item:list){
                    if (Integer.parseInt(item) > tam)
                        tam = Integer.parseInt(item);
                }
                return tam;
            }
        }catch (Exception ex){
            Log.e("Error","List was null");
        }
        return 0;
    }

    private void PreparePieGraph(View view, int year, String TenQuy, int month1, int month2, int month3){
        ////////////////////// TOTAL CHART //////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////
        Log.e("Data", "Year: " + year + ", tên quý: " + TenQuy + ", Tháng đầu: " + month1 + ", tháng giữa: " + month2 + ", tháng cuối: " + month3);
        totalChart = (PieChart) view.findViewById(R.id.piechart1);
        totalChart.setUsePercentValues(true);
        totalChart.getDescription().setEnabled(true);
        totalChart.setExtraOffsets(5, 10, 5, 5);
        totalChart.setDragDecelerationFrictionCoef(0.95f);

        tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-Regular.ttf");

        totalChart.setCenterTextTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-Light.ttf"));
        totalChart.setCenterText(generateCenterSpannableText());
        totalChart.setTransparentCircleColor(R.color.colorAccent);
        totalChart.setCenterTextColor(R.color.basePressColor);
        totalChart.setCenterTextSize(15f);

        totalChart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

        totalChart.setDrawHoleEnabled(true);
        totalChart.setTransparentCircleRadius(30f);
        totalChart.setHoleRadius(30f);
        totalChart.setHoleColor(R.color.rippleEffectColor);

        totalChart.setTransparentCircleColor(R.color.spots_dialog_color);
        totalChart.setTransparentCircleAlpha(110);

        totalChart.setTransparentCircleColor(R.color.warning_stroke_color);
        totalChart.setEntryLabelColor(R.color.baseReleaseColor);
        totalChart.setDrawCenterText(true);
        totalChart.setContentDescription("Biểu đồ thống kê của " + TenQuy);
        totalChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        totalChart.setRotationEnabled(true);
        totalChart.setHighlightPerTapEnabled(true);

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        totalChart.setOnChartValueSelectedListener(this);

        totalChart.animateY(1400, Easing.EaseInOutQuad);
        // chart.spin(2000, 0, 360);

        Legend l = totalChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);

        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
        IntergrateData(totalChart, month1, month2, month3, year + "", TenQuy);
    }

    private void IntergrateData(PieChart chart, int Month1, int Month2, int Month3, String year, String TenQuy){
        float sum = (float) Month1 + (float) Month2 + (float) Month3;
        float party1 =  (Month1/sum)*100;
        float party2 = (Month2/sum)*100;
        float party3 = (Month3/sum)*100;
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((Month1/sum)*100, "Tháng đầu"));
        entries.add(new PieEntry((Month2/sum)*100, "Tháng giữa"));
        entries.add(new PieEntry((Month3/sum)*100, "Tháng cuối"));

        PieDataSet set = new PieDataSet(entries, "Thống kê % theo " + TenQuy);
        set.setValueTextSize(15f);
        set.setColor(R.color.colorAccent);
        set.setFormSize(24f);
        set.setValueLineColor(R.color.colorAccent);
        set.setColors(ColorTemplate.COLORFUL_COLORS);
        set.setSelectionShift(12f);
        set.setUsingSliceColorAsValueLineColor(true);
        set.setSliceSpace(0f);
        set.setValueLineWidth(10f);
        set.setHighlightEnabled(true);
        set.setValueTextSize(28f);

        PieData data = new PieData(set);
        data.setValueFormatter(new PercentFormatter());
        chart.setData(data);
        chart.setCenterText(TenQuy);
        chart.setCenterTextColor(R.color.colorAccent);
        Description des = new Description();
        des.setTextSize(18f);
        des.setText("Thống kê " + TenQuy);
        chart.setDescription(des);
        chart.invalidate(); // refresh
    }

    private void GenerateDataFromCloud(String Quy, final String YearNumberStyle, String key){
        switch (Quy){
            case "Quy1":
                lvMonth1.setAdapter(null);
                lvMonth2.setAdapter(null);
                lvMonth3.setAdapter(null);
                month1.setText("Tháng 1");
                month2.setText("Tháng 2");
                month3.setText("Tháng 3");
                DatabaseReference statistical_tbl = FirebaseDatabase.getInstance().getReference(Common.statistical_tbl);
                statistical_tbl.child(key).child(YearNumberStyle).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        double total1 = 0;
                        double total2 = 0;
                        double total3 = 0;
                        final List<DailyStatistical> statisticals1 = new ArrayList<>();
                        final List<DailyStatistical> statisticals2 = new ArrayList<>();
                        final List<DailyStatistical> statisticals3 = new ArrayList<>();
                        for (DataSnapshot item:snapshots){
                            Log.e("sample data", "" + item.toString());
                            String month = (Integer.parseInt(item.getKey()) + 1) + "";
                            if (month.equals("1")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total1 += model.getTotalFee();
                                    }
                                    statisticals1.add(dailyStatistical);
                                }
                            }
                            if (month.equals("2")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total2 += model.getTotalFee();
                                    }
                                    statisticals2.add(dailyStatistical);
                                }
                            }
                            if (month.equals("3")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total3 += model.getTotalFee();
                                    }
                                    statisticals3.add(dailyStatistical);
                                }
                            }
                            CustomListViewAdapter_forEachDayStatistical adapter = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals1);
                            lvMonth1.setAdapter(adapter);
                            lvMonth1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals1.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });
                            CustomListViewAdapter_forEachDayStatistical adapter1 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals2);
                            lvMonth2.setAdapter(adapter1);
                            lvMonth2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals2.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });
                            CustomListViewAdapter_forEachDayStatistical adapter2 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals3);
                            lvMonth3.setAdapter(adapter2);
                            lvMonth3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals3.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });

                        }
                        Log.e("total", "Tháng 1: " + total1 + ", tháng 2: " + total2 + ", tháng 3: " + total3);
                        TongDoanhThuTrongQuy.setText("$" + (total1 + total2 + total3));
                        PreparePieGraph(dialogView, Integer.parseInt(YearNumberStyle), "Quý 1", (int) total1, (int) total2, (int) total3);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case "Quy2":
                lvMonth1.setAdapter(null);
                lvMonth2.setAdapter(null);
                lvMonth3.setAdapter(null);
                month1.setText("Tháng 4");
                month2.setText("Tháng 5");
                month3.setText("Tháng 6");
                DatabaseReference statistical_tbl2 = FirebaseDatabase.getInstance().getReference(Common.statistical_tbl);
                statistical_tbl2.child(key).child(YearNumberStyle).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        double total1 = 0;
                        double total2 = 0;
                        double total3 = 0;
                        final List<DailyStatistical> statisticals1 = new ArrayList<>();
                        final List<DailyStatistical> statisticals2 = new ArrayList<>();
                        final List<DailyStatistical> statisticals3 = new ArrayList<>();
                        for (DataSnapshot item:snapshots){
                            Log.e("sample data", "" + item.toString());
                            String month = (Integer.parseInt(item.getKey()) + 1) + "";
                            if (month.equals("4")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total1 += model.getTotalFee();
                                    }
                                    statisticals1.add(dailyStatistical);
                                }
                            }
                            if (month.equals("5")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total2 += model.getTotalFee();
                                    }
                                    statisticals2.add(dailyStatistical);
                                }
                            }
                            if (month.equals("6")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total3 += model.getTotalFee();
                                    }
                                    statisticals3.add(dailyStatistical);
                                    Log.e("size of 3", "" + statisticals3.size());
                                }
                            }
                            Log.e("size of 2", "" + statisticals2.size());
                            Log.e("size of 3", "" + statisticals3.size());
                            CustomListViewAdapter_forEachDayStatistical adapter = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals1);
                            lvMonth1.setAdapter(adapter);
                            lvMonth1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals1.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });
                            CustomListViewAdapter_forEachDayStatistical adapter1 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals2);
                            lvMonth2.setAdapter(adapter1);
                            lvMonth2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals2.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });
                            CustomListViewAdapter_forEachDayStatistical adapter2 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals3);
                            lvMonth3.setAdapter(adapter2);
                            lvMonth3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals3.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });
                        }
                        Log.e("total", "Tháng 1: " + total1 + ", tháng 2: " + total2 + ", tháng 3: " + total3);
                        TongDoanhThuTrongQuy.setText("$" + (total1 + total2 + total3));
                        PreparePieGraph(dialogView, Integer.parseInt(YearNumberStyle), "Quý 2", (int) total1, (int) total2, (int) total3);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case "Quy3":
                lvMonth1.setAdapter(null);
                lvMonth2.setAdapter(null);
                lvMonth3.setAdapter(null);
                month1.setText("Tháng 7");
                month2.setText("Tháng 8");
                month3.setText("Tháng 9");
                DatabaseReference statistical_tbl3 = FirebaseDatabase.getInstance().getReference(Common.statistical_tbl);
                statistical_tbl3.child(key).child(YearNumberStyle).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        double total1 = 0;
                        double total2 = 0;
                        double total3 = 0;
                        final List<DailyStatistical> statisticals1 = new ArrayList<>();
                        final List<DailyStatistical> statisticals2 = new ArrayList<>();
                        final List<DailyStatistical> statisticals3 = new ArrayList<>();
                        for (DataSnapshot item:snapshots){
                            Log.e("sample data", "" + item.toString());
                            String month = (Integer.parseInt(item.getKey()) + 1) + "";
                            if (month.equals("7")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total1 += model.getTotalFee();
                                    }
                                    statisticals1.add(dailyStatistical);
                                }
                            }
                            if (month.equals("8")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total2 += model.getTotalFee();
                                    }
                                    statisticals2.add(dailyStatistical);
                                }
                            }
                            if (month.equals("9")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total3 += model.getTotalFee();
                                    }
                                    statisticals3.add(dailyStatistical);
                                }
                            }
                            CustomListViewAdapter_forEachDayStatistical adapter = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals1);
                            lvMonth1.setAdapter(adapter);
                            lvMonth1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals1.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });
                            CustomListViewAdapter_forEachDayStatistical adapter1 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals2);
                            lvMonth2.setAdapter(adapter1);
                            lvMonth2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals2.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });
                            CustomListViewAdapter_forEachDayStatistical adapter2 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals3);
                            lvMonth3.setAdapter(adapter2);
                            lvMonth3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals3.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });
                        }
                        Log.e("total", "Tháng 1: " + total1 + ", tháng 2: " + total2 + ", tháng 3: " + total3);
                        TongDoanhThuTrongQuy.setText("$" + (total1 + total2 + total3));
                        PreparePieGraph(dialogView, Integer.parseInt(YearNumberStyle), "Quý 3", (int) total1, (int) total2, (int) total3);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case "Quy4":
                lvMonth1.setAdapter(null);
                lvMonth2.setAdapter(null);
                lvMonth3.setAdapter(null);
                month1.setText("Tháng 10");
                month2.setText("Tháng 11");
                month3.setText("Tháng 12");
                DatabaseReference statistical_tbl4 = FirebaseDatabase.getInstance().getReference(Common.statistical_tbl);
                statistical_tbl4.child(key).child(YearNumberStyle).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        double total1 = 0;
                        double total2 = 0;
                        double total3 = 0;
                        final List<DailyStatistical> statisticals1 = new ArrayList<>();
                        final List<DailyStatistical> statisticals2 = new ArrayList<>();
                        final List<DailyStatistical> statisticals3 = new ArrayList<>();
                        for (DataSnapshot item:snapshots){
                            Log.e("sample data", "" + item.toString());
                            String month = (Integer.parseInt(item.getKey()) + 1) + "";
                            if (month.equals("10")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total1 += model.getTotalFee();
                                    }
                                    statisticals1.add(dailyStatistical);
                                }
                            }
                            if (month.equals("11")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total2 += model.getTotalFee();
                                    }
                                    statisticals2.add(dailyStatistical);
                                }
                            }
                            if (month.equals("12")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString()) ;
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
                                        dailyStatistical.setFinishedTime("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate() + ", " + model.getCompletedHour() + ": " + model.getCompletedMinutes());
                                        dailyStatistical.setFixedLocation(model.getFixLocation());
                                        dailyStatistical.setServiceFee(model.getServiceFee());
                                        dailyStatistical.setServiceName(model.getServiceName());
                                        dailyStatistical.setServiceVAT(model.getVatFee());
                                        dailyStatistical.setCustomerName(model.getCustomerName());
                                        dailyStatistical.setCustomerPhone(model.getCustomerPhone());
                                        dailyStatistical.setDate("Thứ " + model.getCompletedWeekDate() + ", ngày " +
                                                model.getCompletedMonthDate());
                                        dailyStatistical.setFee(model.getTotalFee());
                                        total3 += model.getTotalFee();
                                    }
                                    statisticals3.add(dailyStatistical);
                                }
                            }
                            CustomListViewAdapter_forEachDayStatistical adapter = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals1);
                            lvMonth1.setAdapter(adapter);
                            lvMonth1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals1.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });
                            CustomListViewAdapter_forEachDayStatistical adapter1 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals2);
                            lvMonth2.setAdapter(adapter1);
                            lvMonth2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals2.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });
                            CustomListViewAdapter_forEachDayStatistical adapter2 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals3);
                            lvMonth3.setAdapter(adapter2);
                            lvMonth3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    DailyStatistical currentItem = statisticals3.get(position);
                                    createStatisticalDetailDialog(currentItem);
                                }
                            });

                        }
                        Log.e("total", "Tháng 1: " + total1 + ", tháng 2: " + total2 + ", tháng 3: " + total3);
                        TongDoanhThuTrongQuy.setText("$" + (total1 + total2 + total3));
                        PreparePieGraph(dialogView, Integer.parseInt(YearNumberStyle), "Quý 4", (int) total1, (int) total2, (int) total3);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
        }
    }

    private SpannableString generateCenterSpannableText() {
        SpannableString s = new SpannableString("From VUANH: chart source code developed by Philipp Jahoda");
        s.setSpan(new RelativeSizeSpan(1.5f), 0, 14, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
        s.setSpan(new RelativeSizeSpan(.65f), 14, s.length() - 15, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
        return s;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void createStatisticalDetailDialog(DailyStatistical item){
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(context);
        dialog.setTitle("Thông tin chi tiết hóa đơn");
        LayoutInflater inflater = this.getLayoutInflater();
        View statisticalDetail = inflater.inflate(R.layout.specific_statistical_dialog, null);

        final TextView txtTotalFee = (TextView) statisticalDetail.findViewById(R.id.txtDetailTotal);
        final TextView txtCustomerName = (TextView) statisticalDetail.findViewById(R.id.txtDetailName);
        final TextView txtCustomerPhone = (TextView) statisticalDetail.findViewById(R.id.customer_phone);
        final TextView txtServiceName = (TextView) statisticalDetail.findViewById(R.id.txtServiceName);
        final TextView txtServiceFee = (TextView) statisticalDetail.findViewById(R.id.txtServiceFee);
        final TextView txt_ServiceVATFee = (TextView) statisticalDetail.findViewById(R.id.txt_ServiceVATFee);
        final TextView txtFix_Location = (TextView) statisticalDetail.findViewById(R.id.txtFix_Location);
        final TextView txtFix_time = (TextView) statisticalDetail.findViewById(R.id.txtFix_time);

        txtTotalFee.setText("$" + item.getFee());
        txtCustomerName.setText(item.getCustomerName());
        txtCustomerPhone.setText(item.getCustomerPhone());
        txtServiceName.setText(item.getServiceName());
        txtServiceFee.setText("$" + item.getServiceFee());
        txt_ServiceVATFee.setText("$" + item.getServiceVAT());
        txtFix_Location.setText(item.getFixedLocation());
        txtFix_time.setText(item.getFinishedTime());

        dialog.setView(statisticalDetail);

        dialog.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }
}
