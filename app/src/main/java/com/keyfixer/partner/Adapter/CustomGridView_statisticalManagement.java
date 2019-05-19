package com.keyfixer.partner.Adapter;

import android.content.Context;
import android.media.Image;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.keyfixer.partner.Model.Fixer;
import com.keyfixer.partner.Model.Statistical;
import com.keyfixer.partner.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomGridView_statisticalManagement extends ArrayAdapter<Fixer> {
    private Context context;
    private List<Fixer> list;
    private int resource;


    public CustomGridView_statisticalManagement(Context context, int resource, List<Fixer> objects) {
        super(context, resource, objects);
        this.context = context;
        this.list = objects;
        this.resource = resource;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Fixer getItem(int position) {
        // TODO Auto-generated method stub
        if (list.get(position) != null)
            return list.get(position);
        return null;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View line;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        line = layoutInflater.inflate(resource, null);

        ImageView fixer_avatar = (ImageView) line.findViewById(R.id.normalFixer_avatar);
        TextView txtfixername = (TextView) line.findViewById(R.id.normal_fixer_username);

        Fixer info = list.get(position);
        Log.e("check info", "" + info.toString());
        if (info != null){
            if (info.getAvatarUrl() != null && !TextUtils.isEmpty(info.getAvatarUrl()))
                Picasso.with(context).load(info.getAvatarUrl()).into(fixer_avatar);
            txtfixername.setText(info.getStrName());
        }

        return line;
    }
}
