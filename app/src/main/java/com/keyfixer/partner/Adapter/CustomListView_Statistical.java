package com.keyfixer.partner.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.keyfixer.partner.Model.Fixer;
import com.keyfixer.partner.Model.Statistical;
import com.keyfixer.partner.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomListView_Statistical extends ArrayAdapter<Statistical> {

    private Context context;
    private int resource;
    private List<Statistical> list;

    public CustomListView_Statistical(Context context, int resource, List<Statistical> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.list = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View line;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        line = layoutInflater.inflate(resource, null);

        TextView txtCustomerName = (TextView) line.findViewById(R.id.txt_customerName);
        TextView txtCustomerAddress = (TextView) line.findViewById(R.id.txt_customerAddress);
        TextView txttotalFee = (TextView) line.findViewById(R.id.txt_totalFee);

        Statistical info = list.get(position);
        Log.e("check info", "" + info.toString());
        if (info != null){
            txtCustomerName.setText(info.getCustomerName());
            txtCustomerAddress.setText(info.getFixLocation());
            txttotalFee.setText("$" + info.getTotalFee());
        }

        return line;
    }
}
