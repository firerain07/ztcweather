package com.tanovo.twaqg.ztcweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.concurrent.locks.Condition;

/**
 * Created by Administrator on 2018/9/17.
 */

public class Daily_forecast {

    @SerializedName("cond_txt_d")
    public String cond_d_Name;

    @SerializedName("cond_code_d")
    public String cond_d_code;

    @SerializedName("cond_txt_n")
    public String cond_n_name;

    @SerializedName("cond_code_n")
    public String cond_n_code;

    public String date;

    @SerializedName("hum")
    public String hum;

    @SerializedName("mr")
    public String mr;

    @SerializedName("ms")
    public String ms;

    @SerializedName("pcpn")
    public String pcpn;


}
