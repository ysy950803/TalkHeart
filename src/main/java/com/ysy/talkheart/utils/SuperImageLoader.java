package com.ysy.talkheart.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lzy.ninegrid.NineGridView;
import com.ysy.talkheart.R;

/**
 * Created by Shengyu Yao on 2017/2/3.
 */

public class SuperImageLoader implements NineGridView.ImageLoader {

    @Override
    public void onDisplayImage(Context context, ImageView imageView, String url) {
        Glide.with(context).load(url)
                .placeholder(R.drawable.ic_default_bg)
                .error(R.drawable.ic_default_bg)
                .into(imageView);
    }

    @Override
    public Bitmap getCacheImage(String url) {
        return null;
    }
}