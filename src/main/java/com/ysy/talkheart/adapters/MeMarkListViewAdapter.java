package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.ninegrid.ImageInfo;
import com.lzy.ninegrid.NineGridView;
import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.MarkActivity;
import com.ysy.talkheart.bases.SuperRecyclerViewAdapter;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.views.CircularImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/23.
 */

public class MeMarkListViewAdapter extends SuperRecyclerViewAdapter {

    private List<String> uidList;
    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> timeList;
    private List<String> textList;
    private List<String> imgInfoList;

    private MarkActivity context;

    private String AVATAR_UPLOAD_URL = "";
    private String IMG_UPLOAD_URL = "";

    public MeMarkListViewAdapter(MarkActivity context, List<String> uidList,
                                 List<Integer> avatarList, List<String> nicknameList,
                                 List<String> timeList, List<String> textList,
                                 List<String> imgInfoList) {
        this.uidList = uidList;
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.timeList = timeList;
        this.textList = textList;
        this.imgInfoList = imgInfoList;
        this.context = context;
        this.AVATAR_UPLOAD_URL = context.getResources().getString(R.string.url_avatar_upload);
        this.IMG_UPLOAD_URL = context.getString(R.string.url_images_upload);
    }

    private static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView timeTv;
        TextView textTv;
        NineGridView gridView;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.me_mark_avatar_img);
            nicknameTv = (TextView) itemView.findViewById(R.id.me_mark_nickname_tv);
            timeTv = (TextView) itemView.findViewById(R.id.me_mark_time_tv);
            textTv = (TextView) itemView.findViewById(R.id.me_mark_text_tv);
            gridView = (NineGridView) itemView.findViewById(R.id.me_mark_gridView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_me_mark, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        RecyclerViewHolder holder = (RecyclerViewHolder) viewHolder;
        downloadAvatar(context, AVATAR_UPLOAD_URL + "/" + uidList.get(position) + "_avatar_img_thumb.jpg",
                holder.avatarImg, avatarList.get(position));
        holder.nicknameTv.setText(nicknameList.get(position));
        holder.timeTv.setText(timeList.get(position));
        holder.textTv.setText(textList.get(position));

        final int pos = Integer.parseInt(position + "");

        holder.avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectionDetector cd = new ConnectionDetector(context);
                if (!cd.isConnectingToInternet())
                    Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                else
                    context.openPerson(pos);
            }
        });

        String imgInfo = imgInfoList.get(position);
        if (imgInfo != null) {
            String[] dateTimeCount = imgInfo.split("_");
            String date = dateTimeCount[0];
            String timePoint = dateTimeCount[1];
            int imgCount = Integer.parseInt(dateTimeCount[2]);
            String uid = uidList.get(position);
            ArrayList<ImageInfo> imageInfos = new ArrayList<>();
            for (int i = 0; i < imgCount; i++) {
                ImageInfo info = new ImageInfo();
                String urlHead = IMG_UPLOAD_URL + "/" + date + "/" + uid +
                        "_" + timePoint + "_active_img_" + i;
                info.setThumbnailUrl(urlHead + "_thumb.jpg");
                info.setBigImageUrl(urlHead + ".jpg");
                imageInfos.add(info);
            }
            holder.gridView.setVisibility(View.VISIBLE);
            holder.gridView.setAdapter(new ImageGridViewAdapter(
                    context, imageInfos));
        } else
            holder.gridView.setVisibility(View.GONE);

        super.onBindViewHolder(viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return nicknameList.size();
    }
}
