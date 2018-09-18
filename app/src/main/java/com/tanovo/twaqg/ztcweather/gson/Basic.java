package com.tanovo.twaqg.ztcweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2018/9/17.
 */

public class Basic {

    @SerializedName("cid")
    public String weatherId;

    @SerializedName("location")
    public String countyName;

    @SerializedName("parent_city")
    public String cityName;

    @SerializedName("admin_area")
    public String provinceName;

    //纬度
    @SerializedName("lat")
    public String latitude;
    //经度
    @SerializedName("lon")
    public String longitude;

    @SerializedName("tz")
    public String timezone;
}
