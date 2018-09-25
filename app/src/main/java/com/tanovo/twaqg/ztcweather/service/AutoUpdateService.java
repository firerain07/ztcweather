package com.tanovo.twaqg.ztcweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tanovo.twaqg.ztcweather.gson.Weather;
import com.tanovo.twaqg.ztcweather.ui.WeatherActivity;
import com.tanovo.twaqg.ztcweather.util.HttpUtil;
import com.tanovo.twaqg.ztcweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updatePic();
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        int timecircle = 8 * 60 * 60 * 100;
        long triggerAtTime = SystemClock.elapsedRealtime() + timecircle; //启动后开始计时
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        alarm.cancel(pi);
        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     *
     */
    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "https://free-api.heweather.com/s6/weather/forecast?location=" + weatherId + "&key=3bdb37009b9f413f9b1f4225555cc6fa";

            HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    Log.i("weather_status", weather.status);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }

    }

    /**
     *
     */
    private void updatePic() {
        String requestUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOKHttpRequest(requestUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }
}
