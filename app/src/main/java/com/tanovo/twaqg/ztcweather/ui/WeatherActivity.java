package com.tanovo.twaqg.ztcweather.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tanovo.twaqg.ztcweather.R;
import com.tanovo.twaqg.ztcweather.gson.Forecast;
import com.tanovo.twaqg.ztcweather.gson.Weather;
import com.tanovo.twaqg.ztcweather.service.AutoUpdateService;
import com.tanovo.twaqg.ztcweather.util.HttpUtil;
import com.tanovo.twaqg.ztcweather.util.Utility;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swRefreshLayouyt;

    private ScrollView weatherLayout;
    private TextView titleCounty;
    private TextView titleText;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private ImageView bingPicImg;
    private Button navButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        weatherLayout=(ScrollView) findViewById(R.id.weather_layout);
        titleCounty=(TextView) findViewById(R.id.title_county);
        titleText=(TextView) findViewById(R.id.title_text);
        titleUpdateTime=(TextView) findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        swRefreshLayouyt=(SwipeRefreshLayout)findViewById(R.id.refresh_layout);
        drawerLayout=(DrawerLayout)findViewById(R.id.draw_layout);
        navButton=(Button)findViewById(R.id.nav_button);

        swRefreshLayouyt.setColorSchemeResources(R.color.colorPrimary);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        final String weatherId;
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null){
            //
            Weather weather= Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            weatherId=getIntent().getStringExtra("weatherId");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }


        /* 设置状态栏透明*/
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        /* 加载背景图片*/
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        weatherLayout.setVisibility(View.INVISIBLE);
        /*刷新天气信息*/
        swRefreshLayouyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
    }

    /**
     *
     */
    public void requestWeather(final String weatherId){
        String weatherUrl="https://free-api.heweather.com/s6/weather/forecast?location="+weatherId+"&key=3bdb37009b9f413f9b1f4225555cc6fa";
        HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"请求天气信息失败",Toast.LENGTH_SHORT).show();
                        swRefreshLayouyt.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                Log.i("weather_status", weather.status);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swRefreshLayouyt.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    private void loadBingPic(){
        String requestUrl="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOKHttpRequest(requestUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    public void showWeatherInfo(Weather weather){
        if (weather!=null&&"ok".equals(weather.status)){
            String countyName=weather.basic.countyName;
            String updateTime=weather.update.localDate;

            titleCounty.setText(countyName);
            titleUpdateTime.setText(updateTime);

            forecastLayout.removeAllViews();
            for (Forecast forecast:weather.forecastList){
                View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
                TextView dateText=(TextView) view.findViewById(R.id.date_text);
                TextView infoText=(TextView)view.findViewById(R.id.info_text);
                TextView maxText=(TextView)view.findViewById(R.id.max_text);
                TextView minText=(TextView)view.findViewById(R.id.min_text);
                dateText.setText(forecast.date);
                infoText.setText(forecast.cond_txt_d);
                maxText.setText(forecast.tmp_max);
                minText.setText(forecast.tmp_min);
                forecastLayout.addView(view);
            }
            weatherLayout.setVisibility(View.VISIBLE);
            Intent intent=new Intent(this, AutoUpdateService.class);
            startService(intent);
        }else {
            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
        }



    }
}
