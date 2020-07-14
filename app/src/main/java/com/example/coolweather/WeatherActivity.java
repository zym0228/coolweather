package com.example.coolweather;


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
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.Httputil;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    private Button navButton;
    private Button setButton;
    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private LinearLayout forecastLayout;
    private ImageView bingPicImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化各个控件
        Log.d("test2","初始化各个控件");
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);

        drawerLayout = (DrawerLayout)findViewById(R.id.draw_layout);
        navButton = (Button)findViewById(R.id.nav_button);
        setButton = (Button)findViewById(R.id.set_button);

        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString != null){
            //解析缓存中的天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else
        {
            mWeatherId = getIntent().getStringExtra("weather_id");
            if(mWeatherId!=null)
            {
                Log.d("test2","weatherId"+mWeatherId);
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            }

        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test","hello!");
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        String bingPic = prefs.getString("bing_Pic",null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId){
        Log.d("weatherId",weatherId);
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=888e1556451241f6857ac188e9fb0e52";
        Log.d("test2",weatherUrl);
        Httputil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                  final String reponseText = response.body().string();
                  final Weather weather = Utility.handleWeatherResponse(reponseText);
                Log.d("test2","reponseText: "+reponseText);
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          if(weather!=null && "ok".equals(weather.status)){
                              SharedPreferences.Editor editor = PreferenceManager.
                                      getDefaultSharedPreferences(WeatherActivity.this).edit();
                              editor.putString("weather",reponseText);
                              editor.apply();
                              mWeatherId = weather.basic.weatherId;
                              showWeatherInfo(weather);
                          } else{
                              Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                              Log.d("test2","onFailure");
                          }
                          swipeRefresh.setRefreshing(false);
                      }
                  });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        Log.d("test2","onFailure2");
                        swipeRefresh.setRefreshing(false);
                    }
              });
            }
        });

        loadBingPic();
    }
    /**
     * 加载必应每日一图
     */
    private void loadBingPic(){
       String requestBingPic = "http://guolin.tech/api/bing_pic";
       Httputil.sendOkHttpRequest(requestBingPic, new Callback() {
           @Override
           public void onFailure(Call call, IOException e) {
               e.printStackTrace();
           }

           @Override
           public void onResponse(Call call, Response response) throws IOException {
              final String bingPic = response.body().string();
              SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
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
    /**
     *处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather){

        Log.d("test2","showWeatherInfo");
          String cityName = weather.basic.cityName;
          String updateTime = weather.basic.update.updateTime.split(" ")[1];
          String degree = weather.now.temperature + "℃";
          String weatherInfo = weather.now.more.info;
          titleCity.setText(cityName);
          titleUpdateTime.setText(updateTime);
          degreeText.setText(degree);
          weatherInfoText.setText(weatherInfo);
        Log.d("test2","showWeatherInfo2");
          forecastLayout.removeAllViews();
          for(Forecast forecast:weather.forecastList){
              View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
              TextView dateText = (TextView) view.findViewById(R.id.date_text);
              TextView infoText = (TextView) view.findViewById(R.id.info_text);
              TextView maxText = (TextView) view.findViewById(R.id.max_text);
              TextView mixText = (TextView) view.findViewById(R.id.min_text);
              dateText.setText(forecast.date);
              infoText.setText(forecast.more.info);
              maxText.setText(forecast.temperature.max);
              mixText.setText(forecast.temperature.min);
              forecastLayout.addView(view);
          }
        Log.d("test2","showWeatherInfo3");
          if(weather.aqi!=null){
              aqiText.setText(weather.aqi.city.aqi);
              pm25Text.setText(weather.aqi.city.pm25);
              Log.d("test2","showWeatherInfo4");
          }
          String comfort = "舒适度:"+weather.suggestion.comfort.info;
          String carWash = "洗车指数"+weather.suggestion.carWash.info;
          String sport = "运动建议"+weather.suggestion.sport.info;

        Log.d("test2","showWeatherInfo5");
          comfortText.setText(comfort);
          carWashText.setText(carWash);
          sportText.setText(sport);
          weatherLayout.setVisibility(View.VISIBLE);

    }
}
