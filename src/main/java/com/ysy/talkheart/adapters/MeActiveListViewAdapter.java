package com.ysy.talkheart.adapters;

import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.ActiveActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

/**
 * Created by Shengyu Yao on 2016/11/24.
 */

public class MeActiveListViewAdapter extends RecyclerView.Adapter<MeActiveListViewAdapter.RecyclerViewHolder> {

    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> timeList;
    private List<String> textList;
    private List<Integer> goodStatusList;
    private List<String> goodNumList;
    private ListOnItemClickListener mOnItemClickListener;
    private ActiveActivity context;
    private byte[] avatarBytes;

    private final int NORMAL_TYPE = R.layout.item_me_active;
    private final int FOOT_TYPE = R.layout.item_foot_loading;
    private int maxExistCount = 9;
    private boolean isLoading = true;

    private FootLoadCallBack loadCallBack;

    public interface FootLoadCallBack {
        void onLoad();
    }

    public MeActiveListViewAdapter(ActiveActivity context, byte[] avatarBytes,
                                   List<Integer> avatarList, List<String> nicknameList,
                                   List<String> timeList, List<String> textList,
                                   List<Integer> goodStatusList, List<String> gooNumList) {
        this.context = context;
        this.avatarBytes = avatarBytes;
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.timeList = timeList;
        this.textList = textList;
        this.goodStatusList = goodStatusList;
        this.goodNumList = gooNumList;
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView timeTv;
        TextView textTv;
        ImageView goodImg;
        TextView goodNumTv;
        ImageView commentImg;

        ProgressBar loadingPBar;
        TextView loadingTv;

        RecyclerViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == R.layout.item_me_active) {
                avatarImg = (CircularImageView) itemView.findViewById(R.id.me_active_avatar_img);
                nicknameTv = (TextView) itemView.findViewById(R.id.me_active_nickname_tv);
                timeTv = (TextView) itemView.findViewById(R.id.me_active_time_tv);
                textTv = (TextView) itemView.findViewById(R.id.me_active_text_tv);
                goodImg = (ImageView) itemView.findViewById(R.id.me_active_good_img);
                goodNumTv = (TextView) itemView.findViewById(R.id.me_active_good_num_tv);
                commentImg = (ImageView) itemView.findViewById(R.id.me_active_comment_img);
            } else {
                loadingPBar = (ProgressBar) itemView.findViewById(R.id.foot_loading_progressbar);
                loadingTv = (TextView) itemView.findViewById(R.id.foot_loading_tv);
            }
        }
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(final RecyclerViewHolder viewHolder, int position) {
        if (getItemViewType(position) == NORMAL_TYPE) {
            if (avatarBytes != null) {
                viewHolder.avatarImg.setImageBitmap(BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length));
            } else
                viewHolder.avatarImg.setImageResource(avatarList.get(position));
            viewHolder.nicknameTv.setText(nicknameList.get(position));
            viewHolder.timeTv.setText(timeList.get(position));
            viewHolder.textTv.setText(textList.get(position));
            final int pos = Integer.parseInt(position + "");

            final TextView goodNumTv = viewHolder.goodNumTv;
            goodNumTv.setText(goodNumList.get(position));
            final ImageView goodImg = viewHolder.goodImg;
            goodImg.setImageResource(goodStatusList.get(position) == 1 ? R.mipmap.ic_favorite_pink_36dp : R.mipmap.ic_favorite_blue_circle_36dp);
            goodImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionDetector cd = new ConnectionDetector(context);
                    if (!cd.isConnectingToInternet()) {
                        Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
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

            ImageView commentImg = viewHolder.commentImg;
            commentImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionDetector cd = new ConnectionDetector(context);
                    if (!cd.isConnectingToInternet()) {
                        Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                    } else {
                        context.openComment(pos);
                    }
                }
            });

            viewHolder.avatarImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectionDetector cd = new ConnectionDetector(context);
                    if (!cd.isConnectingToInternet())
                        Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
                    else
                        context.openPerson(pos);
                }
            });

            // 如果设置了回调，则设置点击事件
            if (mOnItemClickListener != null) {
                viewHolder.itemView.setOnClickListener(new NoDoubleViewClickListener() {
                    @Override
                    protected void onNoDoubleClick(View v) {
                        int pos = viewHolder.getLayoutPosition();
                        mOnItemClickListener.onItemClick(viewHolder.itemView, pos);
                    }
                });

                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int pos = viewHolder.getLayoutPosition();
                        mOnItemClickListener.onItemLongClick(viewHolder.itemView, pos);
                        return false;
                    }
                });
            }
        } else {
            if (isLoading) {
                viewHolder.loadingPBar.setVisibility(View.VISIBLE);
                viewHolder.loadingTv.setText(R.string.content_loading);
                loadCallBack.onLoad();
            } else {
                viewHolder.loadingPBar.setVisibility(View.GONE);
                viewHolder.loadingTv.setText(R.string.content_loading_fail);
                viewHolder.loadingTv.setOnClickListener(new NoDoubleViewClickListener() {
                    @Override
                    protected void onNoDoubleClick(View v) {
                        isLoading = true;
                        viewHolder.loadingPBar.setVisibility(View.VISIBLE);
                        viewHolder.loadingTv.setText(R.string.content_loading);
                        loadCallBack.onLoad();
                    }
                });
            }
        }
    }

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setFootLoadCallBack(FootLoadCallBack loadCallBack) {
        this.loadCallBack = loadCallBack;
    }

    public void setMaxExistCount(int maxExistCount) {
        this.maxExistCount = maxExistCount;
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }
}
