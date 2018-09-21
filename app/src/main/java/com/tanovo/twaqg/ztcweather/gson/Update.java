package com.tanovo.twaqg.ztcweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Administrator on 2018/9/17.
 */

public class Update {

    @SerializedName("loc")
    public String localDate;

    @SerializedName("utc")
    public String  utcDate;
}
