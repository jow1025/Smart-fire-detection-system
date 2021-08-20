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
/////////////////////
import com.github.mikephil.charting.charts.PieChart;
////////////////////
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
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
import com.github.mikephil.charting.charts.PieChart;

/////////////////////////
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
//////////////////////////
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CctvActivity extends AppCompatActivity{
    //EditText editTextEmail;
    //EditText editTextPassword;
    //Button buttonSignup;
    //TextView textviewSingin;
    //TextView textviewMessage;
    //on create 밖에 onclik으로 분기할때는 private 하고 oncreate안에 작성할거면 private지우기
    // oncreate밖에 작성할 때는 calltext.setOnclickListner(this)지우기
    //안에 작성할 때는 calltext.seronclick~~{}
    private Button callButton,cctvOnButton,cctvOffButton;
    private ImageButton carUpButton,carDownButton,carleftButton,carRightButton;
    private Switch LEDswitch,MotorSwitch,BuzzerSwitch;
    private RadioGroup led_radioGroup;
    private RadioButton led_red,led_green,led_blue;
    ProgressDialog progressDialog;

    private DatabaseReference mdust_ref,mgas_ref,mhumid_ref,mtemp_ref;      //push값, 즉 두번째 값부터~
    private DatabaseReference fdust_ref,fgas_ref,fhumid_ref,ftemp_ref;      //set값, 즉 첫번째 값들

    private LineChart Dust_chart,Gas_chart,Temp_chart,Humid_chart;
    private Thread dust_thread,gas_thread,temp_thread,humid_thread;

    private LineData data_dust,data_temp,data_humid,data_gas;
    private ILineDataSet set_dust,set_gas,set_humid,set_temp;

    private TextView f_dust,f_temp,f_humid,f_gas;                           //파이어베이스 set 초기값


    //////////
    private PieChart pie_chart;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        //callText=findViewById(R.id.callText);
        //cctvOnButton=findViewById(R.id.cctvOnButton);
        //cctvOffButton=findViewById(R.id.cctvOffButton);

        //mdust_ref = FirebaseDatabase.getInstance().getReference();  //파이어베이스 래퍼런스


        callButton = (Button) findViewById(R.id.callButton);           //신고하기버튼
        carUpButton = (ImageButton) findViewById(R.id.UpButton);       //DC모터 UP
        carDownButton = (ImageButton) findViewById(R.id.DownButton);   //DC모터 down
        carleftButton = (ImageButton) findViewById(R.id.LeftButton);   //DC모터 left
        carRightButton = (ImageButton) findViewById(R.id.RightButton); //DC모터 right

        //MotorSwitch=(Switch)findViewById(R.id.motorSwitch);
        LEDswitch = (Switch) findViewById(R.id.ledswitch);             //LED 스위치 : 켜짐과 동시에 RGB라디오버튼이 보인다. 꺼지면 초기화됨(체크했던게 다 풀림)
        BuzzerSwitch = (Switch) findViewById(R.id.buzzerSwitch);       //buzzer 스위치

        led_radioGroup = findViewById(R.id.LED_radioGroup);           //라디오그룹
        led_red = findViewById(R.id.led_red);                         //라디오버튼 red
        led_blue = findViewById(R.id.led_blue);                       //라디오버튼 blue
        led_green = findViewById(R.id.led_green);                     //라디오버튼 green

       // Dust_chart = (LineChart) findViewById(R.id.dust_chart);        //미세먼지 차트
        Gas_chart = (LineChart) findViewById(R.id.gas_chart);          //가스 차트
       // Temp_chart = (LineChart) findViewById(R.id.temp_chart);        //온도 차트
       // Humid_chart = (LineChart) findViewById(R.id.humid_chart);      //습도 차트

       // f_dust=(TextView)findViewById(R.id.tv_fdust);
        f_gas=(TextView)findViewById(R.id.tv_fgas);
       // f_humid=(TextView)findViewById(R.id.tv_fhumid);
       /// f_temp=(TextView)findViewById(R.id.tv_ftemp);

        ///////////
       // pie_chart=(PieChart)findViewById(R.id.pi_chart);

        carUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CctvActivity.this,"앞으로 이동",Toast.LENGTH_SHORT).show();
                OkHttpClient client=new OkHttpClient();
                Request request=new Request.Builder()
                        .url("http://192.168.0.26:5000/r_forward")
                        .build();
                client.newCall(request).enqueue(new Callback(){

                    @Override
                    public void onFailure(Call call, IOException e) {
                        call.cancel();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String myResponse=response.body().string();
                        CctvActivity.this.runOnUiThread(new Runnable(){

                            @Override
                            public void run() {
                                //cctvOffButton.setText(myResponse);

                            }
                        });
                    }
                });
            }
        });
        carDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CctvActivity.this,"아래쪽 이동",Toast.LENGTH_SHORT).show();
                OkHttpClient client=new OkHttpClient();
                Request request=new Request.Builder()
                        .url("http://192.168.0.26:5000/r_backward")
                        .build();
                client.newCall(request).enqueue(new Callback(){

                    @Override
                    public void onFailure(Call call, IOException e) {
                        call.cancel();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String myResponse=response.body().string();
                        CctvActivity.this.runOnUiThread(new Runnable(){

                            @Override
                            public void run() {
                                //cctvOffButton.setText(myResponse);

                            }
                        });
                    }
                });
            }
        });
        carleftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CctvActivity.this,"왼쪽 이동",Toast.LENGTH_SHORT).show();
                OkHttpClient client=new OkHttpClient();
                Request request=new Request.Builder()
                        .url("http://192.168.0.26:5000/r_left")
                        .build();
                client.newCall(request).enqueue(new Callback(){

                    @Override
                    public void onFailure(Call call, IOException e) {
                        call.cancel();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String myResponse=response.body().string();
                        CctvActivity.this.runOnUiThread(new Runnable(){

                            @Override
                            public void run() {
                                //cctvOffButton.setText(myResponse);
                            }
                        });
                    }
                });
            }
        });
        carRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CctvActivity.this,"오른쪽 이동",Toast.LENGTH_SHORT).show();
                OkHttpClient client=new OkHttpClient();
                Request request=new Request.Builder()
                        .url("http://192.168.0.26:5000/r_right")
                        .build();
                client.newCall(request).enqueue(new Callback(){

                    @Override
                    public void onFailure(Call call, IOException e) {
                        call.cancel();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String myResponse=response.body().string();
                        CctvActivity.this.runOnUiThread(new Runnable(){

                            @Override
                            public void run() {
                                //cctvOffButton.setText(myResponse);

                            }
                        });
                    }
                });
            }
        });


        //gas 초기값 가져오기
        fgas_ref=FirebaseDatabase.getInstance().getReference("data_a").child("1-set");
        fgas_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                String gas_data = dataSnapshot.getValue().toString();
                Log.d("fgas", gas_data);
                f_gas.setText(gas_data + "µg/m3" );
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });
        WebView webView = (WebView) findViewById(R.id.cctvWeb);
        String url = "http://192.168.0.11:8091/?action=stream";
        webView.loadUrl(url);
        webView.setPadding(0, 0, 0, 0);
        //webView.setInitialScale(100);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        LEDswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                if (isChecked) {
                    led_radioGroup.setVisibility(View.VISIBLE);
                    //buzzerOn();
                    Toast.makeText(CctvActivity.this, "LED켜짐", Toast.LENGTH_SHORT).show();
                } else {
                    led_radioGroup.setVisibility(View.INVISIBLE);
                    Toast.makeText(CctvActivity.this, "LED꺼짐", Toast.LENGTH_SHORT).show();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://192.168.0.26:5000/buzzer_off")
                            .build();
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            call.cancel();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String myResponse = response.body().string();
                            CctvActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    cctvOnButton.setText(myResponse);
                                }
                            });
                        }
                    });

                }
            }
        });

        BuzzerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                if (isChecked) {
                    //buzzerOn();
                    //mDatabase.child("buzzerState").setValue(1);
                    //Toast.makeText(getApplication(),"Buzzer On",Toast.LENGTH_SHORT);

                    Toast.makeText(CctvActivity.this,"auto on",Toast.LENGTH_SHORT).show();
                    OkHttpClient client=new OkHttpClient();
                    Request request=new Request.Builder()
                            .url("http://192.168.0.26:5000/auto_on")
                            .build();
                    client.newCall(request).enqueue(new Callback(){

                        @Override
                        public void onFailure(Call call, IOException e) {
                            call.cancel();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                        }
                    });
                } else {
                    //mDatabase.child("buzzerState").setValue(0);
                    //Toast.makeText(getApplication(),"Buzzer Off",Toast.LENGTH_SHORT);
                    Toast.makeText(CctvActivity.this,"auto off",Toast.LENGTH_SHORT).show();
                    OkHttpClient client=new OkHttpClient();
                    Request request=new Request.Builder()
                            .url("http://192.168.0.26:5000/auto_off")
                            .build();
                    client.newCall(request).enqueue(new Callback(){

                        @Override
                        public void onFailure(Call call, IOException e) {
                            call.cancel();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                        }
                    });
                }
            }
        });


        led_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.led_blue) {
                    //mDatabase.child("led_State").child("BLUE").setValue(1);
                    //mDatabase.child("led_State").child("RED").setValue(0);
                    //mDatabase.child("led_State").child("GREEN").setValue(0);
                } else if (i == R.id.led_red) {
                    //mDatabase.child("led_State").child("BLUE").setValue(0);
                    // mDatabase.child("led_State").child("RED").setValue(1);
                    //mDatabase.child("led_State").child("GREEN").setValue(0);
                } else {
                    //mDatabase.child("led_State").child("BLUE").setValue(0);
                    //mDatabase.child("led_State").child("RED").setValue(0);
                    // mDatabase.child("led_State").child("GREEN").setValue(1);
                }
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CctvActivity.this);
                builder.setTitle("신고");
                builder.setMessage("신고하시겠습니까?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:119"));
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
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


        //////////////////





    }
    //oncreateFinishLine///////////////////////////////////////////////////////////////////////////////////


    public void buzzerOn(){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url("http://192.168.0.26:5000/buzzer_on")
                .build();
        client.newCall(request).enqueue(new Callback(){

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String myResponse=response.body().string();
                CctvActivity.this.runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        cctvOnButton.setText(myResponse);

                    }
                });
            }

        });
    }

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


                    /////////////

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
}



