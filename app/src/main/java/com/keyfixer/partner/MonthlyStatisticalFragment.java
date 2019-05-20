package com.keyfixer.partner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.facebook.accountkit.ui.LoginFlowState;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.keyfixer.partner.Adapter.CustomListViewAdapter_forEachDayStatistical;
import com.keyfixer.partner.Adapter.CustomListView_Statistical;
import com.keyfixer.partner.Common.Common;
import com.keyfixer.partner.Model.DailyStatistical;
import com.keyfixer.partner.Model.Statistical;

import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthlyStatisticalFragment extends Fragment
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, MaterialSpinner.OnItemSelectedListener, OnChartValueSelectedListener {

    private static final int PERMISSION_STORAGE = 0;

    protected Typeface tfRegular;
    protected Typeface tfLight;
    PieChart totalChart;
    View view;
    Context context;
    MaterialSpinner spinner;
    RadioButton quy1, quy2, quy3, quy4;
    private Typeface tf;
    TextView month1, month2, month3, TongDoanhThuTrongQuy;
    ListView lvMonth1, lvMonth2, lvMonth3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.weeklystatistical_fragment, null);
        this.context = container.getContext();
        Initializing(view);
        tfRegular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-Regular.ttf");
        tfLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-Light.ttf");
        return view;
    }

    private void Initializing(View view) {
        TongDoanhThuTrongQuy = (TextView) view.findViewById(R.id.TongDoanhThuCuaQuy);
        month1 = (TextView) view.findViewById(R.id.Month1);
        month2 = (TextView) view.findViewById(R.id.Month2);
        month3 = (TextView) view.findViewById(R.id.Month3);
        lvMonth1 = (ListView) view.findViewById(R.id.FirstMonth);
        lvMonth2 = (ListView) view.findViewById(R.id.SecondMonth);
        lvMonth3 = (ListView) view.findViewById(R.id.ThirdMonth);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int date = Calendar.getInstance().get(Calendar.DATE);
        PrepareSpinner(view);
        int index = spinner.getSelectedIndex();
        final String CURRENT_YEAR = spinner.getItems().get(index).toString();
        GenerateDataFromCloud("Quy2", CURRENT_YEAR);
        quy1 = (RadioButton) view.findViewById(R.id.quy1);
        quy2 = (RadioButton) view.findViewById(R.id.quy2);
        quy3 = (RadioButton) view.findViewById(R.id.quy3);
        quy4 = (RadioButton) view.findViewById(R.id.quy4);

        if (quy1.isChecked()) {
            GenerateDataFromCloud("Quy1", CURRENT_YEAR);
        }
        if (quy2.isChecked()) {
            GenerateDataFromCloud("Quy2", CURRENT_YEAR);
        }
        if (quy3.isChecked()) {
            GenerateDataFromCloud("Quy3", CURRENT_YEAR);
        }
        if (quy4.isChecked()) {
            GenerateDataFromCloud("Quy4", CURRENT_YEAR);
        }

        quy1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateDataFromCloud("Quy1", CURRENT_YEAR);
                quy1.setChecked(true);
            }
        });
        quy2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateDataFromCloud("Quy2", CURRENT_YEAR);
                quy2.setChecked(true);
            }
        });
        quy3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateDataFromCloud("Quy3", CURRENT_YEAR);
                quy3.setChecked(true);
            }
        });
        quy4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateDataFromCloud("Quy4", CURRENT_YEAR);
                quy4.setChecked(true);
            }
        });
    }

    private void PrepareSpinner(View view){
        spinner = (MaterialSpinner) view.findViewById(R.id.Yearspinner);
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
        spinner.setOnItemSelectedListener(this);
    }

    private void PreparePieGraph(View view, int year, String TenQuy, int month1, int month2, int month3){
        ////////////////////// TOTAL CHART //////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////
        Log.e("Data", "Year: " + year + ", tên quý: " + TenQuy + ", Tháng đầu: " + month1 + ", tháng giữa: " + month2 + ", tháng cuối: " + month3);
        totalChart = (PieChart) view.findViewById(R.id.piechart);
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
        Log.e("Sample data", "month 1: " + Month1 + ", month2: " + Month2 + ", month3: " + Month3);
        int sum = Month1 + Month2 + Month3;
        int party1 = 0, party2 = 0, party3 = 0;
        if (sum != 0){
            party1 = (int) Month1/sum;
            party2 = (int) Month2/sum;
            party3 = (int) Month3/sum;
        }
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(party1, "Tháng đầu"));
        entries.add(new PieEntry(party2, "Tháng giữa"));
        entries.add(new PieEntry(party3, "Tháng cuối"));

        PieDataSet set = new PieDataSet(entries, "Thống kê % theo " + TenQuy);
        set.setValueTextSize(15f);
        set.setColor(R.color.colorAccent);
        set.setFormSize(24f);
        set.setValueLineColor(R.color.colorAccent);
        set.setColors(ColorTemplate.COLORFUL_COLORS);
        set.setSelectionShift(12f);
        set.setUsingSliceColorAsValueLineColor(true);
        set.setSliceSpace(16f);
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

    private void GenerateDataFromCloud(String Quy, final String YearNumberStyle){
        switch (Quy){
            case "Quy1":
                lvMonth1.setAdapter(null);
                lvMonth2.setAdapter(null);
                lvMonth3.setAdapter(null);
                month1.setText("Tháng 1");
                month2.setText("Tháng 2");
                month3.setText("Tháng 3");
                DatabaseReference statistical_tbl = FirebaseDatabase.getInstance().getReference(Common.statistical_tbl);
                statistical_tbl.child(Common.FixerID).child(YearNumberStyle).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        double total1 = 0;
                        double total2 = 0;
                        double total3 = 0;
                        List<DailyStatistical> statisticals1 = new ArrayList<>();
                        List<DailyStatistical> statisticals2 = new ArrayList<>();
                        List<DailyStatistical> statisticals3 = new ArrayList<>();
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
                            CustomListViewAdapter_forEachDayStatistical adapter1 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals2);
                            lvMonth2.setAdapter(adapter1);
                            CustomListViewAdapter_forEachDayStatistical adapter2 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals3);
                            lvMonth3.setAdapter(adapter2);

                        }
                        Log.e("total", "Tháng 1: " + total1 + ", tháng 2: " + total2 + ", tháng 3: " + total3);
                        TongDoanhThuTrongQuy.setText("$" + (total1 + total2 + total3));
                        PreparePieGraph(view, Integer.parseInt(YearNumberStyle), "Quý 1", (int) total1, (int) total2, (int) total3);
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
                statistical_tbl2.child(Common.FixerID).child(YearNumberStyle).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        double total1 = 0;
                        double total2 = 0;
                        double total3 = 0;
                        for (DataSnapshot item:snapshots){
                            Log.e("sample data", "" + item.toString());
                            String month = (Integer.parseInt(item.getKey()) + 1) + "";
                            List<DailyStatistical> statisticals1 = new ArrayList<>();
                            List<DailyStatistical> statisticals2 = new ArrayList<>();
                            List<DailyStatistical> statisticals3 = new ArrayList<>();
                            if (month.equals("4")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
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
                            CustomListViewAdapter_forEachDayStatistical adapter1 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals2);
                            lvMonth2.setAdapter(adapter1);
                            CustomListViewAdapter_forEachDayStatistical adapter2 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals3);
                            lvMonth3.setAdapter(adapter2);
                        }
                        Log.e("total", "Tháng 1: " + total1 + ", tháng 2: " + total2 + ", tháng 3: " + total3);
                        TongDoanhThuTrongQuy.setText("$" + (total1 + total2 + total3));
                        PreparePieGraph(view, Integer.parseInt(YearNumberStyle), "Quý 2", (int) total1, (int) total2, (int) total3);
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
                statistical_tbl3.child(Common.FixerID).child(YearNumberStyle).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        double total1 = 0;
                        double total2 = 0;
                        double total3 = 0;
                        for (DataSnapshot item:snapshots){
                            Log.e("sample data", "" + item.toString());
                            String month = (Integer.parseInt(item.getKey()) + 1) + "";
                            List<DailyStatistical> statisticals1 = new ArrayList<>();
                            List<DailyStatistical> statisticals2 = new ArrayList<>();
                            List<DailyStatistical> statisticals3 = new ArrayList<>();
                            if (month.equals("7")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
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
                            CustomListViewAdapter_forEachDayStatistical adapter1 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals2);
                            lvMonth2.setAdapter(adapter1);
                            CustomListViewAdapter_forEachDayStatistical adapter2 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals3);
                            lvMonth3.setAdapter(adapter2);
                        }
                        Log.e("total", "Tháng 1: " + total1 + ", tháng 2: " + total2 + ", tháng 3: " + total3);
                        TongDoanhThuTrongQuy.setText("$" + (total1 + total2 + total3));
                        PreparePieGraph(view, Integer.parseInt(YearNumberStyle), "Quý 3", (int) total1, (int) total2, (int) total3);
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
                statistical_tbl4.child(Common.FixerID).child(YearNumberStyle).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        double total1 = 0;
                        double total2 = 0;
                        double total3 = 0;
                        for (DataSnapshot item:snapshots){
                            Log.e("sample data", "" + item.toString());
                            String month = (Integer.parseInt(item.getKey()) + 1) + "";
                            List<DailyStatistical> statisticals1 = new ArrayList<>();
                            List<DailyStatistical> statisticals2 = new ArrayList<>();
                            List<DailyStatistical> statisticals3 = new ArrayList<>();
                            if (month.equals("10")){
                                Iterable<DataSnapshot> snapshots1 = item.getChildren();
                                for (DataSnapshot item1:snapshots1){
                                    Log.e("sample data", "" + item1.toString());
                                    DailyStatistical dailyStatistical = new DailyStatistical();
                                    Iterable<DataSnapshot> snapshots2 = item1.getChildren();
                                    for (DataSnapshot item2:snapshots2){
                                        Log.e("sample data", "" + item2.toString());
                                        Statistical model = item2.getValue(Statistical.class);
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
                            CustomListViewAdapter_forEachDayStatistical adapter1 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals2);
                            lvMonth2.setAdapter(adapter1);
                            CustomListViewAdapter_forEachDayStatistical adapter2 = new CustomListViewAdapter_forEachDayStatistical(context, R.layout.customlistview_statistical_for_eachday, statisticals3);
                            lvMonth3.setAdapter(adapter2);

                        }
                        Log.e("total", "Tháng 1: " + total1 + ", tháng 2: " + total2 + ", tháng 3: " + total3);
                        TongDoanhThuTrongQuy.setText("$" + (total1 + total2 + total3));
                        PreparePieGraph(view, Integer.parseInt(YearNumberStyle), "Quý 4", (int) total1, (int) total2, (int) total3);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
        }
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

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
}
