//package com.example.project;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.PersistableBundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.ProgressBar;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.ListResult;
//import com.google.firebase.storage.StorageReference;
//
//import java.util.ArrayList;
//
//public class StorageActivity extends AppCompatActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_storage);
//        final ProgressBar progressBar=findViewById(R.id.progressBar);
//        final ArrayList<String> imageList=new ArrayList<>();
//        final RecyclerView recyclerView=findViewById(R.id.recyclerView);
//        final ImageAdapter adapter= new ImageAdapter(imageList,this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        //부드럽게 스크롤
//        recyclerView.setNestedScrollingEnabled(false);
//        StorageReference storageRef= FirebaseStorage.getInstance().getReference().child("image_store");
//
//        progressBar.setVisibility(View.VISIBLE);
//        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
//            @Override
//            public void onSuccess(ListResult listResult) {
//                for(StorageReference fileRef:listResult.getItems()){
//                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            imageList.add(uri.toString());
//                            Log.d("item",uri.toString());
//
//                        }
//                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            recyclerView.setAdapter(adapter);
//                            progressBar.setVisibility(View.GONE);
//                        }
//                    });
////
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onBackPressed() {
//        startActivity(new Intent(this, ProfileActivity.class));
//    }
//}
