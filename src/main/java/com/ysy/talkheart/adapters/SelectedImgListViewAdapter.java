package com.ysy.talkheart.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.SuperRecyclerViewAdapter;
import com.ysy.talkheart.utils.AndroidLifecycleUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Shengyu Yao on 2017/1/29.
 */

public class SelectedImgListViewAdapter extends SuperRecyclerViewAdapter {

    private ArrayList<String> imagesPath;
    private Context mContext;

    public SelectedImgListViewAdapter(Context context, ArrayList<String> imagesPath) {
        this.imagesPath = imagesPath;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_selected_img, parent, false));
    }

    @Override
    public int getItemCount() {
        return imagesPath.size();
    }

    private static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView selectedImg;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            selectedImg = (ImageView) itemView.findViewById(R.id.selected_img);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        RecyclerViewHolder holder = (RecyclerViewHolder) viewHolder;
        Uri uri = Uri.fromFile(new File(imagesPath.get(position)));
        boolean canLoadImg = AndroidLifecycleUtils.canLoadImage(holder.selectedImg.getContext());
        if (canLoadImg) {
            Glide.with(mContext).load(uri)
                    .centerCrop()
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                    .error(R.drawable.__picker_ic_broken_image_black_48dp)
                    .into(holder.selectedImg);
        }

        super.onBindViewHolder(viewHolder, position);
    }
}
