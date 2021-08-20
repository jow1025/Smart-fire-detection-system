//package com.example.project;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.squareup.picasso.Picasso;
//
//import java.util.ArrayList;
//
//public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
//
//    private ArrayList<String> imageList;
//    private Context context;
//
//    public ImageAdapter(ArrayList<String> imageList, Context context) {
//        this.imageList=imageList;
//        this.context=context;
//    }
//
//    @NonNull
//    @Override
//    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
//
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
//        Picasso.get().load(imageList.get(position)).into(holder.imageView);
//    }
//
//    @Override
//    public int getItemCount() {
//        return imageList.size();
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//
//        ImageView imageView;
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            imageView=itemView.findViewById(R.id.imageView);
//        }
//    }
//}