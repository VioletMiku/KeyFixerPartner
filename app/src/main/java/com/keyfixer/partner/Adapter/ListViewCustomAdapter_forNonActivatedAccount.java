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
import com.keyfixer.partner.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListViewCustomAdapter_forNonActivatedAccount extends ArrayAdapter<Fixer> {
    private Context context;
    private int resource;
    private List<Fixer> listAccount;

    public ListViewCustomAdapter_forNonActivatedAccount(Context context , int resource , List<Fixer> objects) {
        super(context , resource , objects);
        this.resource = resource;
        this.listAccount = objects;
    }

    @Override
    public View getView(int position,View convertView, ViewGroup parent) {
        View line;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        line = layoutInflater.inflate(resource, null);

        CircleImageView avatar_background = (CircleImageView)line.findViewById(R.id.avatar);
        TextView txtFixerName = (TextView) line.findViewById(R.id.fixer_name);
        TextView txtFixerEmail = (TextView) line.findViewById(R.id.fixer_email);
        TextView txtFixerPhone = (TextView) line.findViewById(R.id.fixer_phone);

        Fixer info = listAccount.get(position);
        Log.e("check info", "" + info.toString());
        if (info != null){
            if (info.getAvatarUrl() != null && !TextUtils.isEmpty(info.getAvatarUrl()))
                Picasso.with(getContext()).load(info.getAvatarUrl()).into(avatar_background);
            txtFixerName.setText(info.getStrName());
            txtFixerEmail.setText(info.getStrEmail());
            String phoneNumber = info.getStrPhone().substring(0,4) + " " + info.getStrPhone().substring(5,7) + " " + info.getStrPhone().substring(8);
            txtFixerPhone.setText(phoneNumber);
        }

        return line;
    }
}
