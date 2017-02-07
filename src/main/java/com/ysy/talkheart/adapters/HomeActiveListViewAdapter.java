package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.ninegrid.ImageInfo;
import com.lzy.ninegrid.NineGridView;
import com.ysy.talkheart.R;
import com.ysy.talkheart.bases.SuperRecyclerViewAdapter;
import com.ysy.talkheart.fragments.HomeFragment;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表适配器
 * Created by Shengyu Yao on 2016/7/7.
 */

public class HomeActiveListViewAdapter extends SuperRecyclerViewAdapter {

    private String AVATAR_UPLOAD_URL = "";
    private String IMG_UPLOAD_URL = "";

    private List<String> uidList;
    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> timeList;
    private List<String> textList;
    private List<Integer> goodStatusList;
    private List<String> goodNumList;
    private List<String> imgInfoList;
    private HomeFragment context;

    private final int NORMAL_TYPE = R.layout.item_home_active;
    private final int FOOT_TYPE = R.layout.item_foot_loading;

    public HomeActiveListViewAdapter(HomeFragment context, List<String> uidList,
                                     List<Integer> avatarList, List<String> nicknameList,
                                     List<String> timeList, List<String> textList,
                                     List<Integer> goodStatusList, List<String> gooNumList,
                                     List<String> imgInfoList) {
        this.context = context;
        this.uidList = uidList;
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.timeList = timeList;
        this.textList = textList;
        this.goodStatusList = goodStatusList;
        this.goodNumList = gooNumList;
        this.imgInfoList = imgInfoList;
        this.AVATAR_UPLOAD_URL = context.getString(R.string.url_avatar_upload);
        this.IMG_UPLOAD_URL = context.getString(R.string.url_images_upload);
    }

    private static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView timeTv;
        TextView textTv;
        ImageView commentImg;
        ImageView goodImg;
        TextView goodNumTv;
        NineGridView gridView;

        ProgressBar loadingPBar;
        TextView loadingTv;

        RecyclerViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == R.layout.item_home_active) {
                avatarImg = (CircularImageView) itemView.findViewById(R.id.home_active_avatar_img);
                nicknameTv = (TextView) itemView.findViewById(R.id.home_active_nickname_tv);
                timeTv = (TextView) itemView.findViewById(R.id.home_active_time_tv);
                textTv = (TextView) itemView.findViewById(R.id.home_active_text_tv);
                goodImg = (ImageView) itemView.findViewById(R.id.home_active_good_img);
                goodNumTv = (TextView) itemView.findViewById(R.id.home_active_good_num_tv);
                commentImg = (ImageView) itemView.findViewById(R.id.home_active_comment_img);
                gridView = (NineGridView) itemView.findViewById(R.id.home_active_gridView);
            } else {
                loadingPBar = (ProgressBar) itemView.findViewById(R.id.foot_loading_progressbar);
                loadingTv = (TextView) itemView.findViewById(R.id.foot_loading_tv);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        if (viewType == FOOT_TYPE)
            return new RecyclerViewHolder(view, FOOT_TYPE);
        else
            return new RecyclerViewHolder(view, NORMAL_TYPE);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < maxExistCount)
            return NORMAL_TYPE;
        if (position == getItemCount() - 1)
            return FOOT_TYPE;
        else
            return NORMAL_TYPE;
    }

    @Override
    public int getItemCount() {
        return nicknameList.size() < (maxExistCount + 1) ? nicknameList.size() : nicknameList.size() + 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final RecyclerViewHolder holder = (RecyclerViewHolder) viewHolder;
        if (getItemViewType(position) == NORMAL_TYPE) {
            downloadAvatar(context.getContext(),
                    AVATAR_UPLOAD_URL + "/" + uidList.get(position) + "_avatar_img_thumb.jpg",
                    holder.avatarImg, avatarList.get(position));
            holder.nicknameTv.setText(nicknameList.get(position));
            holder.timeTv.setText(timeList.get(position));
            holder.textTv.setText(textList.get(position));
            final int pos = Integer.parseInt(position + "");

            final TextView goodNumTv = holder.goodNumTv;
            goodNumTv.setText(goodNumList.get(position));
            final ImageView goodImg = holder.goodImg;
            goodImg.setImageResource(goodStatusList.get(position) == 1 ? R.mipmap.ic_favorite_pink_36dp : R.mipmap.ic_favorite_blue_circle_36dp);
            goodImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionDetector cd = new ConnectionDetector(context.getActivity());
                    if (!cd.isConnectingToInternet()) {
                        Toast.makeText(context.getActivity(), "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                    } else {
                        context.goodImg = goodImg;
                        context.goodImg.setClickable(false);
                        int goodNum = Integer.parseInt(goodNumTv.getText().toString());
                        if (goodStatusList.get(pos) == 0 || goodStatusList.get(pos) == -1) {
                            String goodNumStr = (++goodNum) + "";
                            goodImg.setImageResource(R.mipmap.ic_favorite_pink_36dp);
                            goodNumTv.setText(goodNumStr);
                            goodNumList.set(pos, goodNumStr);
                            context.updateGood(pos);
                        } else { // 1
                            --goodNum;
                            String goodNumStr = (goodNum < 0 ? 0 : goodNum) + "";
                            goodImg.setImageResource(R.mipmap.ic_favorite_blue_circle_36dp);
                            goodNumTv.setText(goodNumStr);
                            goodNumList.set(pos, goodNumStr);
                            context.updateGood(pos);
                        }
                    }
                }
            });

            ImageView commentImg = holder.commentImg;
            commentImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionDetector cd = new ConnectionDetector(context.getActivity());
                    if (!cd.isConnectingToInternet())
                        Toast.makeText(context.getActivity(), "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                    else
                        context.openComment(pos);
                }
            });

            holder.avatarImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionDetector cd = new ConnectionDetector(context.getActivity());
                    if (!cd.isConnectingToInternet())
                        Toast.makeText(context.getActivity(), "请检查网络连接哦", Toast.LENGTH_SHORT).show();
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
                        context.getContext(), imageInfos));
            } else
                holder.gridView.setVisibility(View.GONE);

            super.onBindViewHolder(viewHolder, position);
        } else {
            if (isLoading) {
                holder.loadingPBar.setVisibility(View.VISIBLE);
                holder.loadingTv.setText(R.string.content_loading);
                loadCallBack.onLoad();
            } else {
                holder.loadingPBar.setVisibility(View.GONE);
                holder.loadingTv.setText(R.string.content_loading_fail);
                holder.loadingTv.setOnClickListener(new NoDoubleViewClickListener() {
                    @Override
                    protected void onNoDoubleClick(View v) {
                        isLoading = true;
                        holder.loadingPBar.setVisibility(View.VISIBLE);
                        holder.loadingTv.setText(R.string.content_loading);
                        loadCallBack.onLoad();
                    }
                });
            }
        }
    }
}
