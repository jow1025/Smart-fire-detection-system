package com.example.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "ProfileActivity";

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //view objects
    private TextView textViewUserEmail;
    private ImageButton buttonLogout;
   // private TextView textviewDelete;
    private ImageButton buttonCctv;
    private ImageButton buttonStorage;
    private ImageButton buttonAnal;

    private TextView timeStatus;
    private TextView nowStatus;
    private TextView dataStatus;
    private DatabaseReference gas_ref;
    private Integer target=3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //initializing views
       // textViewUserEmail = (TextView) findViewById(R.id.textviewUserEmail);
        buttonLogout = (ImageButton) findViewById(R.id.logout);
       // textviewDelete = (TextView) findViewById(R.id.textviewDelete);
        buttonCctv=(ImageButton) findViewById(R.id.cctv);
        buttonStorage=(ImageButton) findViewById(R.id.storage);
        buttonAnal=(ImageButton)findViewById(R.id.analysis);

        // evnet_storage=(Button)findViewById(R.id.event_storage);
        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();

        timeStatus=(TextView)findViewById(R.id.time_text);
        nowStatus=(TextView)findViewById(R.id.status_text);
        dataStatus=(TextView)findViewById(R.id.data_text);
        gas_ref= FirebaseDatabase.getInstance().getReference("data_a").child("1-set");
        gas_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String gas_data=snapshot.getValue().toString();
                Log.d("fgas",gas_data);
                dataStatus.setText(gas_data+"ug/m3");
                if(Integer.parseInt(gas_data)>=target){
                    nowStatus.setText("나쁨(BAD)");
                    nowStatus.setTextColor(Color.RED);

                }
                else {
                    nowStatus.setText("좋음(GOOD)");
                    nowStatus.setTextColor(Color.GREEN);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("TAG", "database error", error.toException());
            }
        });
        //유저가 로그인 하지 않은 상태라면 null 상태이고 이 액티비티를 종료하고 로그인 액티비티를 연다.
        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        //유저가 있다면, null이 아니면 계속 진행
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //textViewUserEmail의 내용을 변경해 준다.
        //textViewUserEmail.setText("반갑습니다.\n"+ user.getEmail()+"으로 로그인 하였습니다.");

        showTime();

        //logout button event
        buttonLogout.setOnClickListener(this);
        //textviewDelete.setOnClickListener(this);
        buttonCctv.setOnClickListener(this);
        buttonStorage.setOnClickListener(this);
        buttonAnal.setOnClickListener(this);


    }

    public void showTime() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                timeStatus.setTextColor(Color.GRAY);
                timeStatus.setText(DateFormat.getDateTimeInstance().format(new Date()));

            }
        };
        Runnable task = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    handler.sendEmptyMessage(1);
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }
    @Override
    public void onClick(View view) {
        if (view == buttonLogout) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        //회원탈퇴를 클릭하면 회원정보를 삭제한다. 삭제전에 컨펌창을 하나 띄워야 겠다.
//        if(view == textviewDelete) {
//            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(ProfileActivity.this);
//            alert_confirm.setMessage("정말 계정을 삭제 할까요?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                            user.delete()
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            Toast.makeText(ProfileActivity.this, "계정이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
//                                            finish();
//                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                                        }
//                                    });
//                        }
//                    }
//            );
//            alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    Toast.makeText(ProfileActivity.this, "취소", Toast.LENGTH_LONG).show();
//                }
//            });
//            alert_confirm.show();
//        }
        if(view==buttonCctv) {
            //TODO
            startActivity(new Intent(this, CctvActivity.class)); //추가해 줄  액티비티

        }
        if(view==buttonStorage){
            finish();
            //startActivity(new Intent(this, StorageActivity.class));
            startActivity(new Intent(this,PostsListActivity.class));
        }
        if(view==buttonAnal){
            finish();
            startActivity(new Intent(this, DataActivity.class));
        }
    }
}
