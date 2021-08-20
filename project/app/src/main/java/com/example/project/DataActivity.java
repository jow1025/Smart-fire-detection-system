package com.example.project;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DataActivity extends AppCompatActivity{


    ProgressDialog progressDialog;

    private DatabaseReference mdust_ref,mgas_ref,mhumid_ref,mtemp_ref;      //push값, 즉 두번째 값부터~
    private DatabaseReference fdust_ref,fgas_ref,fhumid_ref,ftemp_ref;      //set값, 즉 첫번째 값들

    private LineChart Dust_chart,Gas_chart,Temp_chart,Humid_chart;
    private Thread dust_thread,gas_thread,temp_thread,humid_thread;

    private LineData data_dust,data_temp,data_humid,data_gas;
    private ILineDataSet set_dust,set_gas,set_humid,set_temp;

    private TextView f_dust,f_temp,f_humid,f_gas;                           //파이어베이스 set 초기값


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cctv_record);

        Gas_chart = (LineChart) findViewById(R.id.gas_chart);          //가스 차트
        f_gas=(TextView)findViewById(R.id.tv_fgas);

        //gas 초기값 가져오기
        fgas_ref=FirebaseDatabase.getInstance().getReference("data_a").child("1-set");
        fgas_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                String gas_data = dataSnapshot.getValue().toString();
                Log.d("fgas", gas_data);
                f_gas.setText(gas_data);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });


        Gas_chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        Gas_chart.getAxisRight().setEnabled(false);
        Gas_chart.getLegend().setTextColor(Color.WHITE);
        Gas_chart.animateXY(2000, 2000);
        Gas_chart.invalidate();
        LineData data3 = new LineData();
        Gas_chart.setData(data3);
        DrawingGraph_gas();
    }
    //oncreateFinishLine///////////////////////////////////////////////////////////////////////////////////



    private void addEntry_gas(){

        data_gas=Gas_chart.getData();

        if(data_gas!=null){
            set_gas=data_gas.getDataSetByIndex(0);

            if(set_gas==null){
                set_gas=createGasSet();
                data_gas.addDataSet(set_gas);
            }

            //데이터 가져오기
            mgas_ref=FirebaseDatabase.getInstance().getReference("data_a").child("2-push");

            ChildEventListener mChild=new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(snapshot==null){
                        return;
                    }
                    String dust_data = snapshot.getValue().toString();
                    Log.d("dust", dust_data);
                    //data_dust.addEntry(new Entry(set_dust.getEntryCount(),Float.parseFloat(dust_data)),0);
                    data_gas.addEntry(new Entry(set_gas.getEntryCount(),Float.parseFloat(dust_data)),0);
                    data_gas.notifyDataChanged();
                    Gas_chart.notifyDataSetChanged();
                    Gas_chart.setVisibleXRangeMaximum(500);
                    Gas_chart.moveViewToX(data_gas.getDataSetCount());
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };


            mgas_ref.addChildEventListener(mChild);
        }
    }


    private LineDataSet createGasSet(){
        LineDataSet set = new LineDataSet(null, "Gas Data");
        set.setFillAlpha(110);
        set.setFillColor(Color.parseColor("#d7e7fa"));
        set.setColor(Color.parseColor("#0B80C9"));
        set.setCircleColor(Color.parseColor("#FFA1B4DC"));
        set.setCircleHoleColor(Color.BLUE);
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false); set.setLineWidth(2);
        set.setCircleRadius(6); set.setDrawCircleHole(false);
        set.setDrawCircles(false); set.setValueTextSize(9f);
        set.setDrawFilled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setLabel("ppm");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));

        return set;
    }

    private void DrawingGraph_gas(){
        if(gas_thread!=null){
            gas_thread.interrupt();
        }
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                addEntry_gas();
            }
        };

        gas_thread=new Thread(new Runnable(){
            @Override
            public void run() {
                while(true){
                    runOnUiThread(runnable);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        gas_thread.start();
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ProfileActivity.class));
    }
}



