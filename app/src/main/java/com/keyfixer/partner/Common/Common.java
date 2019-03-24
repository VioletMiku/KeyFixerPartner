package com.keyfixer.partner.Common;

import com.keyfixer.partner.Remote.IGoogleAPI;
import com.keyfixer.partner.Remote.RetrofitClient;

public class Common {

    public static final String fixer_tbl = "Fixers";
    public static final String fixer_inf_tbl = "Users";
    public static final String customer_tbl = "Customers";
    public static final String fix_request_tbl = "FixRequest";

    public static final String baseUrl = "https://maps.googleapis.com";

    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseUrl).create(IGoogleAPI.class);
    }
}
