package com.ysy.talkheart.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.ysy.talkheart.R;
import com.ysy.talkheart.activities.ActiveActivity;
import com.ysy.talkheart.activities.DraftActivity;
import com.ysy.talkheart.activities.FansActivity;
import com.ysy.talkheart.im.activities.HomeActivity;
import com.ysy.talkheart.activities.MarkActivity;
import com.ysy.talkheart.activities.PersonActivity;
import com.ysy.talkheart.activities.WatchActivity;
import com.ysy.talkheart.bases.GlobalApp;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.DataProcessor;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.utils.StringUtils;
import com.ysy.talkheart.utils.ViewTurnAnimation;
import com.ysy.talkheart.views.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class MeFragment extends StatedFragment {

    private String AVATAR_UPLOAD_URL = "";

    private List<TextView> tvList = new ArrayList<>();
    private List<TextView> tipTvList = new ArrayList<>();
    private List<CardView> cardList = new ArrayList<>();
    private List<View> viewList = new ArrayList<>();

    @BindView(R.id.me_avatar_img)
    CircularImageView avatarImg;
    @BindView(R.id.me_nickname_tv)
    TextView nicknameTv;
    @BindView(R.id.me_active_num_tv)
    TextView activeNumTv;
    @BindView(R.id.me_active_layout)
    LinearLayout activeNumLayout;
    @BindView(R.id.me_watch_num_tv)
    TextView watchNumTv;
    @BindView(R.id.me_watch_layout)
    LinearLayout watchNumLayout;
    @BindView(R.id.me_fans_num_tv)
    TextView fansNumTv;
    @BindView(R.id.me_fans_layout)
    LinearLayout fansNumLayout;
    @BindView(R.id.me_introduction_tv)
    TextView introductionTv;
    @BindView(R.id.me_mark_layout)
    LinearLayout markLayout;
    @BindView(R.id.me_draft_layout)
    LinearLayout draftLayout;
    @BindView(R.id.me_day_night_layout)
    LinearLayout dayNightLayout;
    @BindView(R.id.me_exit_layout)
    LinearLayout exitLayout;
    @BindView(R.id.me_intro_input_layout)
    LinearLayout introInputLayout;
    @BindView(R.id.me_intro_edt)
    AppCompatEditText introEdt;
    @BindView(R.id.me_intro_done_img)
    ImageView introDoneImg;
    @BindView(R.id.me_night_tv)
    TextView nightTv;
    @BindView(R.id.me_day_tv)
    TextView dayTv;
    private String NICKNAME = "加载中…";
    private String INTRODUCTION = "加载中…";
    private String SEX = "1";
    private String ACTIVE_NUM = "0";
    private String WATCH_NUM = "0";
    private String FANS_NUM = "0";
    private static final String FRAGMENT_TAG = "Me";
    private static final String OPTS_KEY = "opts_o";
    private String UID;
    private HomeActivity context;
    private Handler meFragmentHandler;
    @BindView(R.id.me_refresh_layout)
    SwipeRefreshLayout refreshLayout;
    private boolean isRefreshing = false;
    private String[] opts_o;
    private byte[] avatarBytes;

    public MeFragment() {
        meFragmentHandler = new Handler();
    }

    public static MeFragment newInstance(String tag, String[] opts_o) {
        MeFragment fragment = new MeFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_TAG, tag);
        args.putStringArray(OPTS_KEY, opts_o);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            opts_o = getArguments().getStringArray(OPTS_KEY);
            UID = getArguments().getString(FRAGMENT_TAG);
            context = (HomeActivity) getActivity();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        initData();
        initView(view);
        clickListener();
        refreshLayout.setRefreshing(true);
        refresh();
        return view;
    }

    private void initData() {
        AVATAR_UPLOAD_URL = context.getResources().getString(R.string.url_avatar_upload);
        isRefreshing = false;
    }

    private void initView(View view) {
        ButterKnife.bind(this, view);

        CardView mainInfoCard = (CardView) view.findViewById(R.id.me_main_info_card);
        CardView introCard = (CardView) view.findViewById(R.id.me_introduction_card);
        CardView othersCard = (CardView) view.findViewById(R.id.me_others_card);
        CardView lastCard = (CardView) view.findViewById(R.id.me_last_card);
        TextView activeTv = (TextView) view.findViewById(R.id.me_active_tv);
        TextView watchTv = (TextView) view.findViewById(R.id.me_watch_tv);
        TextView fansTv = (TextView) view.findViewById(R.id.me_fans_tv);
        TextView markTv = (TextView) view.findViewById(R.id.me_mark_tv);
        TextView draftTv = (TextView) view.findViewById(R.id.me_draft_tv);

        View divOthers = view.findViewById(R.id.me_div_others);
        View divLast = view.findViewById(R.id.me_div_last);

        tvList.add(nicknameTv);
        tvList.add(introductionTv);
        tvList.add(activeNumTv);
        tvList.add(watchNumTv);
        tvList.add(fansNumTv);
        tvList.add(markTv);
        tvList.add(draftTv);
        tvList.add(nightTv);
        tvList.add(dayTv);

        tipTvList.add(activeTv);
        tipTvList.add(watchTv);
        tipTvList.add(fansTv);

        cardList.add(mainInfoCard);
        cardList.add(introCard);
        cardList.add(othersCard);
        cardList.add(lastCard);

        viewList.add(divOthers);
        viewList.add(divLast);

        introInputLayout.setVisibility(View.GONE);
        introductionTv.setVisibility(View.VISIBLE);

        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void clickListener() {
        avatarImg.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(context, PersonActivity.class);
                if (avatarBytes != null) {
                    intent.putExtra("avatar", avatarBytes);
                }
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("opts_o", opts_o);
                intent.putExtra("sex", SEX);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), avatarImg, getString(R.string.trans_me_avatar));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        nicknameTv.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(context, PersonActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("opts_o", opts_o);
                startActivity(intent);
            }
        });

        activeNumLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(context, ActiveActivity.class);
                if (avatarBytes != null) {
                    intent.putExtra("avatar", avatarBytes);
                }
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("sex", SEX);
                intent.putExtra("nickname", NICKNAME);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), activeNumTv, getString(R.string.trans_active));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        watchNumLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(context, WatchActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), watchNumTv, getString(R.string.trans_watch));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        fansNumLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(context, FansActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("e_uid", UID);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), fansNumTv, getString(R.string.trans_fans));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        final ViewTurnAnimation anim_intro = new ViewTurnAnimation(introductionTv, introInputLayout);
        introductionTv.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                introductionTv.startAnimation(anim_intro.getSATo(0));
                introEdt.setText(introductionTv.getText().toString().equals("点击设置签名") ? "" : introductionTv.getText().toString());
                introEdt.requestFocus();
            }
        });

        introDoneImg.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                introInputLayout.startAnimation(anim_intro.getSATo(0));
                String intro = introEdt.getText().toString();
                if (intro.length() > 24) {
                    Toast.makeText(context, "签名不能超过24个字符哦", Toast.LENGTH_SHORT).show();
                } else {
                    if (!intro.equals(introductionTv.getText().toString()) && !StringUtils.replaceBlank(introEdt.getText().toString()).equals(""))
                        connectToUpdateIntro(intro);
                }
            }
        });

        markLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(context, MarkActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), markLayout, getString(R.string.trans_mark));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        draftLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                Intent intent = new Intent(context, DraftActivity.class);
                intent.putExtra("uid", UID);
                intent.putExtra("opts_o", opts_o);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions tAO = ActivityOptions.makeSceneTransitionAnimation(getActivity(), draftLayout, getString(R.string.trans_draft));
                    startActivity(intent, tAO.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        final ViewTurnAnimation anim_day_night;
        DataProcessor dP = new DataProcessor(context);
        if (dP.readIntData("day_night") == 1)
            anim_day_night = new ViewTurnAnimation(dayTv, nightTv);
        else
            anim_day_night = new ViewTurnAnimation(nightTv, dayTv);
        dayNightLayout.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                if (nightTv.getVisibility() == View.VISIBLE) {
                    nightTv.startAnimation(anim_day_night.getSATo(0));
                    context.toggleDayNightMode();
                } else {
                    dayTv.startAnimation(anim_day_night.getSATo(0));
                    context.toggleDayNightMode();
                }
            }
        });

        exitLayout.setOnTouchListener(new View.OnTouchListener() {
            long lastTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - lastTime > 5000) {
                            Toast.makeText(getActivity(), "强制更新已开启", Toast.LENGTH_SHORT).show();
                            context.forceCheckUpdate();
                        } else {
                            context.exitDialog();
                        }
                        break;
                }
                return true;
            }
        });
    }

    public void getMeInfo() {
        refreshLayout.setRefreshing(true);
        refresh();
    }

    private void refresh() {
        if (!isRefreshing) {
            isRefreshing = true;
            if (!refreshData()) {
                refreshLayout.setRefreshing(false);
                isRefreshing = false;
            }
        }
    }

    private boolean refreshData() {
        ConnectionDetector cd = new ConnectionDetector(getActivity());
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(context, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        downloadAvatar();
        connectToGetMeInfo(UID);
        return true;
    }

    private void connectToGetMeInfo(final String uid) {
        setClickable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    meFragmentHandler.post(timeOutRunnable);
                } else {
                    String[] res = dbP.meInfoSelect(
                            "select sex, nickname, intro, act_num, watch_num, fans_num from user u, user_info_count uic" +
                                    " where u.uid = " + uid + " and u.uid = uic.uid");
                    if (res[1] == null || res[1].equals("/(ㄒoㄒ)/~~")) {
                        meFragmentHandler.post(serverErrorRunnable);
                    } else {
                        SEX = res[0];
                        NICKNAME = res[1];
                        INTRODUCTION = res[2] == null ? "点击设置签名" : res[2];
                        ACTIVE_NUM = res[3];
                        WATCH_NUM = res[4];
                        FANS_NUM = res[5];
                        meFragmentHandler.post(successRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void connectToUpdateIntro(final String intro) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    meFragmentHandler.post(timeOutRunnable);
                } else {
                    int res = dbP.update(
                            "update user set intro = '" + intro + "' where uid = " + UID
                    );
                    if (res == 1) {
                        meFragmentHandler.post(updateIntroRunnable);
                    } else {
                        meFragmentHandler.post(serverErrorRunnable);
                    }
                }
                dbP.closeConn();
            }
        }).start();
    }

    private void setClickable(boolean isAble) {
        nicknameTv.setClickable(isAble);
        activeNumLayout.setClickable(isAble);
        watchNumLayout.setClickable(isAble);
        fansNumLayout.setClickable(isAble);
        introductionTv.setClickable(isAble);
        markLayout.setClickable(isAble);
        draftLayout.setClickable(isAble);
    }

    private void setAvatarClickable(boolean isAble) {
        avatarImg.setClickable(isAble);
    }

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            if (avatarBytes == null)
                avatarImg.setImageResource(SEX.equals("1") ? R.drawable.me_avatar_boy : R.drawable.me_avatar_girl);
            nicknameTv.setText(new StringUtils().shortNickname(NICKNAME));
            introductionTv.setText(INTRODUCTION);
            activeNumTv.setText(ACTIVE_NUM);
            watchNumTv.setText(WATCH_NUM);
            fansNumTv.setText(FANS_NUM);
            setClickable(true);
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable updateIntroRunnable = new Runnable() {
        @Override
        public void run() {
            introductionTv.setText(introEdt.getText().toString());
            INTRODUCTION = introEdt.getText().toString();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            if (MeFragment.this.isAdded())
                Toast.makeText(context, "服务器君生病了，重试一下吧", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (MeFragment.this.isAdded())
                Toast.makeText(context, "连接超时啦，重试一下吧", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    };

    public void refreshFragmentUI() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        Resources res = getResources();

        theme.resolveAttribute(R.attr.colorDivider, typedValue, true);
        for (View view : viewList)
            view.setBackgroundResource(typedValue.resourceId);

        theme.resolveAttribute(R.attr.colorEdtText, typedValue, true);
        introEdt.setTextColor(res.getColor(typedValue.resourceId));
        theme.resolveAttribute(R.attr.colorHint, typedValue, true);
        introEdt.setHintTextColor(res.getColor(typedValue.resourceId));
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        introEdt.setSupportBackgroundTintList(ColorStateList.valueOf(res.getColor(typedValue.resourceId)));

        theme.resolveAttribute(R.attr.colorPlainText, typedValue, true);
        for (TextView textView : tvList)
            textView.setTextColor(res.getColor(typedValue.resourceId));

        theme.resolveAttribute(R.attr.colorTipText, typedValue, true);
        for (TextView textView : tipTvList)
            textView.setTextColor(res.getColor(typedValue.resourceId));

        theme.resolveAttribute(R.attr.colorPlaintViewBG, typedValue, true);
        for (CardView cardView : cardList)
            cardView.setCardBackgroundColor(res.getColor(typedValue.resourceId));
    }

    private void downloadAvatar() {
        setAvatarClickable(false);
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(16 * 1000);
        httpClient.get(AVATAR_UPLOAD_URL + "/" + UID + "_avatar_img.jpg",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Bitmap picBmp = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                        if (picBmp != null) {
                            avatarBytes = responseBody;
                            avatarImg.setImageBitmap(picBmp);
                            ((GlobalApp) getActivity().getApplication()).setMeInfoUpdated(false);
                        } else {
                            avatarBytes = null;
                        }
                        setAvatarClickable(true);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        avatarBytes = null;
                        setAvatarClickable(true);
                    }
                });
    }
}
