package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.ActiveActivity;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.ListOnItemClickListener;
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

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public MeActiveListViewAdapter(ActiveActivity context, List<Integer> avatarList, List<String> nicknameList,
                                   List<String> timeList, List<String> textList,
                                   List<Integer> goodStatusList, List<String> gooNumList) {
        this.context = context;
        this.avatarList = avatarList;
        this.nicknameList = nicknameList;
        this.timeList = timeList;
        this.textList = textList;
        this.goodStatusList = goodStatusList;
        this.goodNumList = gooNumList;
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircularImageView avatarImg;
        TextView nicknameTv;
        TextView timeTv;
        TextView textTv;
        ImageView goodImg;
        TextView goodNumTv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.me_active_avatar_img);
            nicknameTv = (TextView) itemView.findViewById(R.id.me_active_nickname_tv);
            timeTv = (TextView) itemView.findViewById(R.id.me_active_time_tv);
            textTv = (TextView) itemView.findViewById(R.id.me_active_text_tv);
            goodImg = (ImageView) itemView.findViewById(R.id.me_active_good_img);
            goodNumTv = (TextView) itemView.findViewById(R.id.me_active_good_num_tv);
        }
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_me_active, parent, false));
    }

    @Override
    public int getItemCount() {
        return nicknameList.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder viewHolder, int position) {
        viewHolder.avatarImg.setImageResource(avatarList.get(position));
        viewHolder.nicknameTv.setText(nicknameList.get(position));
        viewHolder.timeTv.setText(timeList.get(position));
        viewHolder.textTv.setText(textList.get(position));

        final TextView goodNumTv = viewHolder.goodNumTv;
        goodNumTv.setText(goodNumList.get(position));

        final int pos = Integer.parseInt(position + "");
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
                        String goodNumStr = (--goodNum) + "";
                        goodImg.setImageResource(R.mipmap.ic_favorite_blue_circle_36dp);
                        goodNumTv.setText(goodNumStr);
                        goodNumList.set(pos, goodNumStr);
                        context.updateGood(pos);
                    }
                }
            }
        });

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
    }

}
