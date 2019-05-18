package com.keyfixer.partner.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.keyfixer.partner.Model.DailyStatistical;
import com.keyfixer.partner.R;

import java.util.List;

public class CustomListViewAdapter_forEachDayStatistical extends ArrayAdapter<DailyStatistical> {
    private Context context;
    private int resource;
    private List<DailyStatistical> listFee;

    public CustomListViewAdapter_forEachDayStatistical(Context context , int resource , List<DailyStatistical> objects) {
        super(context , resource , objects);
        this.resource = resource;
        this.listFee = objects;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View line;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        line = layoutInflater.inflate(resource, null);

        TextView txtDate = (TextView) line.findViewById(R.id.day);
        TextView txtTotal = (TextView) line.findViewById(R.id.total);

        DailyStatistical info = listFee.get(position);
        Log.e("check info", "" + info.toString());
        if (info != null){
            txtDate.setText(info.getDate());
            txtTotal.setText(info.getFee() + "");
        }

        return line;
    }
}
