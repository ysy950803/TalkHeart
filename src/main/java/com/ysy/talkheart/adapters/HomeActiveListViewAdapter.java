package com.ysy.talkheart.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ysy.talkheart.R;
import com.ysy.talkheart.fragments.HomeFragment;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.views.CircularImageView;

import java.util.List;

/**
 * 列表适配器
 * Created by Shengyu Yao on 2016/7/7.
 */

public class HomeActiveListViewAdapter extends RecyclerView.Adapter<HomeActiveListViewAdapter.RecyclerViewHolder> {

    private List<Integer> avatarList;
    private List<String> nicknameList;
    private List<String> timeList;
    private List<String> textList;
    private List<Integer> goodStatusList;
    private List<String> goodNumList;
    private ListOnItemClickListener mOnItemClickListener;
    private HomeFragment context;

    public void setListOnItemClickListener(ListOnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public HomeActiveListViewAdapter(HomeFragment context, List<Integer> avatarList, List<String> nicknameList,
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
        ImageView commentImg;
        ImageView goodImg;
        TextView goodNumTv;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircularImageView) itemView.findViewById(R.id.home_active_avatar_img);
            nicknameTv = (TextView) itemView.findViewById(R.id.home_active_nickname_tv);
            timeTv = (TextView) itemView.findViewById(R.id.home_active_time_tv);
            textTv = (TextView) itemView.findViewById(R.id.home_active_text_tv);
            goodImg = (ImageView) itemView.findViewById(R.id.home_active_good_img);
            goodNumTv = (TextView) itemView.findViewById(R.id.home_active_good_num_tv);
            commentImg = (ImageView) itemView.findViewById(R.id.home_active_comment_img);
        }
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_home_active, parent, false));
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
        final int pos = Integer.parseInt(position + "");

        final TextView goodNumTv = viewHolder.goodNumTv;
        goodNumTv.setText(goodNumList.get(position));
        final ImageView goodImg = viewHolder.goodImg;
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

        ImageView commentImg = viewHolder.commentImg;
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

        viewHolder.avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectionDetector cd = new ConnectionDetector(context.getActivity());
                if (!cd.isConnectingToInternet())
                    Toast.makeText(context.getActivity(), "请检查网络连接哦", Toast.LENGTH_SHORT).show();
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
    }
}