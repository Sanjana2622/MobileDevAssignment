package com.example.photogallery;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * RecyclerView adapter to display a grid of images.
 * Uses Glide for efficient loading, caching, and downsampling.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Uri uri);
    }

    private final List<Uri> imageUris;
    private final OnItemClickListener listener;

    public ImageAdapter(List<Uri> imageUris, OnItemClickListener listener) {
        this.imageUris = imageUris;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = imageUris.get(position);

        // Load thumbnail via Glide (handles async, caching, memory)
        Glide.with(holder.imageView.getContext())
                .load(uri)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(uri));
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}