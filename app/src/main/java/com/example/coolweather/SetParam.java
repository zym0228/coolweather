package com.example.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;


public class SetParam extends Fragment {

    private Button btSet;
    private EditText serviceTime_edit;
    private Switch serviceOn_Switch;
    private boolean service_on = false;

    private SharedPreferences mSp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.setpage,container,false);
        btSet = (Button) view.findViewById(R.id.button_submit);
       serviceTime_edit = (EditText)view.findViewById(R.id.data1_txt);
       serviceOn_Switch = (Switch)view.findViewById(R.id.switch_updatedata);

        mSp=getActivity().getSharedPreferences("data",MODE_PRIVATE);
        readInfo(view);
        return view;
    }

    @Override
   public void onActivityCreated(Bundle savedInstanceState){
       super.onActivityCreated(savedInstanceState);
       btSet.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

                 writeInfo(v);


               if (service_on==true){
                   AutoUpdateService.servicetime = Integer.parseInt(serviceTime_edit.getText().toString()) ;
                   Intent  intent = new Intent(getActivity(),AutoUpdateService.class);
                   getActivity().startService(intent);
                   Log.d("test","ServiceOn");
               }else if (service_on==false){
                   Intent  intent = new Intent(getActivity(),AutoUpdateService.class);
                   getActivity().stopService(intent);
                   Log.d("test","ServiceOff");
               }

               Log.d("test", serviceTime_edit.getText().toString());
               WeatherActivity activity = (WeatherActivity)getActivity();
               activity.drawerLayout.closeDrawers();
           }
       });

        serviceOn_Switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(serviceOn_Switch.isChecked())
                {
                    service_on = true;
                    Log.d("test","on");

                }else if(serviceOn_Switch.isChecked()==false){
                    service_on = false;
                    Log.d("test","off");
                }
            }
        });
    }


    //存数据
    public void writeInfo(View view) {
        SharedPreferences.Editor editor=mSp.edit();
        editor.putString("time",serviceTime_edit.getText().toString());
        editor.commit();
        //editor.apply();
        Toast.makeText(getActivity(),"存储时间数据成功",Toast.LENGTH_LONG).show();
    }
    //读数据
    public void readInfo(View view) {
        String info = mSp.getString("time","0");
        serviceTime_edit.setText(info);
        Toast.makeText(getActivity(), "读取时间数据成功", Toast.LENGTH_LONG).show();
    }

}
