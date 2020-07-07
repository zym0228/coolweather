package com.example.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.Httputil;
import com.example.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_RPOVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView)view.findViewById(R.id.title_text);
        backButton = (Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        Log.d("test","Fragmentcreateview");
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_RPOVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity =cityList.get(position);
                    queryCounties();
                }

            }
        });

        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }

                Log.d("test","FragmentsetOnClickListener");
            }
        });
        Log.d("test","FragmentonActivityCreated");
        queryProvinces();
    }

    private void queryProvinces(){

        try{
            Log.d("test","queryProvinces");

            titleText.setText("中国");
            backButton.setVisibility(View.GONE);
            Log.d("test"," backButton.setVisibility");

            provinceList = DataSupport.findAll(Province.class);
            Log.d("test","  provinceList");
            if(provinceList.size()>0){
                dataList.clear();
                for(Province province:provinceList){
                    dataList.add(province.getProvinceName());
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_RPOVINCE;
            }else {
                String address = "http://guolin.tech/api/china";
                queryFromServer(address,"province");
            }
        }catch (Exception e){

            //Log.d("test", );
            Log.d("error", e.toString());
        }

    }
    private void queryCities(){
        Log.d("test", "queryCities");
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("Provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProviceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProviceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }
    private void queryFromServer(String address,final String type){
        Log.d("test","queryFromServer "+type);
        showProgressDialog();
        Httputil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                 String reponseText = response.body().string();
                 boolean result = false;
                 if ("province".equals(type)){
                     Log.d("test","provincereponseText");
                     Log.d("test",reponseText);
                     result = Utility.handleProvinceResponse(reponseText);
                 }else  if("city".equals(type)){
                     Log.d("test","cityreponseText");
                     Log.d("test",reponseText);
                     result = Utility.handleCityResponse(reponseText,selectedProvince.getId());
                 }else if("county".equals(type)){
                     Log.d("test","countyreponseText");
                     Log.d("test",reponseText);
                     result = Utility.handleCountyResponse(reponseText,selectedCity.getId());
                 }
                 if (result){
                     Log.d("test","result");
                     getActivity().runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             closeProgressDialog();
                             if("province".equals(type)){
                                 queryProvinces();
                             }else if("city".equals(type)){
                                 queryCities();
                             }else if("county".equals(type)){
                                 queryCounties();
                             }
                         }
                     });
                 }
            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载…");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog(){
        Log.d("test","closeProgressDialog");
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}