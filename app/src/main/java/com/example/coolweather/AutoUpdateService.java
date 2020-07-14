package com.example.coolweather;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.Httputil;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public static int servicetime = 0;
    AlarmManager manager;
    public AutoUpdateService() {
    }
    @Override
    public void onCreate()
    {
        Log.d("test","servicecreate");
         manager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Log.d("test","onStartCommand");
        updateWeather();
        updateBingPic();

        int anHour =servicetime * 1000;
        Log.d("test","更新数据时间： "+ anHour);
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
       // return super.onStartCommand(intent,flags,startId);
        return START_NOT_STICKY;
    }
    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString!=null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;

            String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId + "&key=888e1556451241f6857ac188e9fb0e52";
            Httputil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                 String reponseText = response.body().string();
                 Weather weather = Utility.handleWeatherResponse(reponseText);
                 if(weather!=null && "ok".equals(weather.status)){
                     SharedPreferences.Editor editor = PreferenceManager.
                             getDefaultSharedPreferences(AutoUpdateService.this).edit();
                     editor.putString("weather",reponseText);
                     editor.apply();

                 }
                }
            });
        }
    }
    private void updateBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        Httputil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.d("test","停止服务");
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);

        manager.cancel(pi);
    }
}
