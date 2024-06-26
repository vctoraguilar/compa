package com.rasgo.compa.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rasgo.compa.R;

import java.util.List;


public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private Context context;
    private List<Uri> photoUris;

    public PhotoAdapter(Context context, List<Uri> photoUris) {
        this.context = context;
        this.photoUris = photoUris;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri photoUri = photoUris.get(position);
        Glide.with(context).load(photoUri).into(holder.photo);
    }

    @Override
    public int getItemCount() {
        return photoUris.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photo;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.photo);
        }
    }
}
