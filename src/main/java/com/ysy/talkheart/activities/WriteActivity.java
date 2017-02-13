package com.ysy.talkheart.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ysy.albumselector.ImageSelector;
import com.ysy.talkheart.R;
import com.ysy.talkheart.adapters.SelectedImgListViewAdapter;
import com.ysy.talkheart.bases.DayNightActivity;
import com.ysy.talkheart.bases.GlobalApp;
import com.ysy.talkheart.utils.ConnectionDetector;
import com.ysy.talkheart.utils.DBProcessor;
import com.ysy.talkheart.utils.ListOnItemClickListener;
import com.ysy.talkheart.utils.NoDoubleMenuItemClickListener;
import com.ysy.talkheart.utils.NoDoubleViewClickListener;
import com.ysy.talkheart.utils.NoDouleDialogClickListener;
import com.ysy.talkheart.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnMultiCompressListener;

public class WriteActivity extends DayNightActivity {

    private EditText writeEdt;
    private TextView restWordTv;
    private static final int WORD_LIMIT = 144;
    private Handler writeHandler;
    private ProgressDialog waitDialog;
    private String UID;
    private String DFT_ID;
    private String DFT_CONTENT = "";
    private ArrayList<String> imagesPath = new ArrayList<>();
    private ArrayList<Integer> imagePos = new ArrayList<>();
    private SelectedImgListViewAdapter listViewAdapter;

    private String IMAGES_UPLOAD_URL = "";
    private String IMAGES_DEL_URL = "";
    private String TIME_POINT = "";
    private RequestParams params;
    private String CONTENT = "";

    private boolean isImgUploaded = false;
    private String imgInfo = null; // 2017/12/21_20171221182345_9

    private boolean isSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        setupActionBar(true);
        initData();
        initView();
        writeHandler = new Handler();
    }

    private void initData() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        TIME_POINT = year + "" + month + "" + day + "" + hour + "" + min + "" + sec;

        IMAGES_UPLOAD_URL = getString(R.string.url_images_upload);
        IMAGES_DEL_URL = getString(R.string.url_images_del);
        UID = getIntent().getExtras().getString("uid");
        DFT_ID = getIntent().getExtras().getString("dft_id");
        DFT_CONTENT = getIntent().getExtras().getString("dft_content", "");
    }

    private void initView() {
        writeEdt = (EditText) findViewById(R.id.write_edt);
        ImageView albumImg = (ImageView) findViewById(R.id.write_album_img);
        restWordTv = (TextView) findViewById(R.id.write_word_tv);

        writeEdt.addTextChangedListener(tw);
        writeEdt.setText(DFT_CONTENT);

        albumImg.setOnClickListener(new NoDoubleViewClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                if (!isImgUploaded)
                    ImageSelector.getInstance()
                            .setSelectModel(ImageSelector.MULTI_MODE)
                            .setMaxCount(9)
                            .setGridColumns(3)
                            .setShowCamera(false)
                            .startSelect(WriteActivity.this, dayNight, imagesPath, imagePos);
                else
                    Toast.makeText(WriteActivity.this, "图片已成功上传，无需更改", Toast.LENGTH_SHORT).show();
            }
        });

        RecyclerView selectedImgRecyclerView = (RecyclerView) findViewById(R.id.write_selected_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        selectedImgRecyclerView.setLayoutManager(layoutManager);
        listViewAdapter = new SelectedImgListViewAdapter(this, imagesPath);
        listViewAdapter.setListOnItemClickListener(new ListOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!isImgUploaded)
                    removeSelectedImg(position);
                else
                    Toast.makeText(WriteActivity.this, "图片已成功上传，无需更改", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        selectedImgRecyclerView.setAdapter(listViewAdapter);
    }

    private boolean send(final String uid, final String content) {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        connectToSend(uid, content);
        return true;
    }

    private boolean save(final int uid, final String content) {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            Toast.makeText(this, "请检查网络连接哦", Toast.LENGTH_SHORT).show();
            return false;
        }
        connectToSave(uid, content);
        return true;
    }

    private void connectToSend(final String uid, String content) {
        if (imagesPath != null && imagesPath.size() > 0 && !isImgUploaded) {
            CONTENT = content;
            List<File> fileList = new ArrayList<>();
            final List<int[]> imgWHList = new ArrayList<>();
            for (int i = 0; i < imagesPath.size(); i++) {
                fileList.add(new File(imagesPath.get(i)));
                imgWHList.add(getImgWH(imagesPath.get(i)));
            }

            Luban.compress(this, fileList)
                    .putGear(Luban.THIRD_GEAR)
                    .launch(new OnMultiCompressListener() {
                        @Override
                        public void onStart() {
                            waitDialog = ProgressDialog.show(WriteActivity.this, "请稍后", "正在处理图片……");
                        }

                        @Override
                        public void onSuccess(List<File> compressFileList) {
                            params = new RequestParams();
                            int i;
                            for (i = 0; i < compressFileList.size(); i++) {
                                byte[] bytes = fileToBytes(compressFileList.get(i));
                                if (bytes != null) {
                                    if (imgWHList.get(i)[0] < 96 && imgWHList.get(i)[1] < 96) { // little img
                                        params.put("multi_img_" + i, new ByteArrayInputStream(bytes),
                                                uid + "_" + TIME_POINT + "_active_img_" + i + "_thumb.jpg", "multipart/form-data");
                                    } else {
                                        params.put("multi_img_" + i, new ByteArrayInputStream(bytes),
                                                uid + "_" + TIME_POINT + "_active_img_" + i + ".jpg", "multipart/form-data");
                                    }
                                } else
                                    break;
                            }
                            waitDialog.dismiss();
                            if (i == compressFileList.size())
                                writeHandler.post(imgUploadRunnable);
                            else
                                writeHandler.post(compressErrorRunnable);
                        }

                        @Override
                        public void onError(Throwable e) {
                            waitDialog.dismiss();
                            writeHandler.post(compressErrorRunnable);
                        }
                    });
        } else {
            sendText(uid, content, imgInfo);
        }
    }

    private void sendText(final String uid, final String content, final String imgInfo) {
        waitDialog = ProgressDialog.show(WriteActivity.this, "请稍后", "正在通知数据库君……");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    writeHandler.post(timeOutRunnable);
                } else {
                    int res;
                    if (imgInfo == null) {
                        res = dbP.insert(
                                "insert into active(uid, sendtime, goodnum, content) values(" +
                                        uid + ", NOW(), 0, '" + content + "')"
                        );
                    } else {
                        res = dbP.insert(
                                "insert into active(uid, sendtime, goodnum, content, img_info) values(" +
                                        uid + ", NOW(), 0, '" + content + "', '" + imgInfo + "')"
                        );
                    }
                    if (res == 1) {
                        if (dbP.update("update user_info_count set act_num = (act_num + 1) where uid = " + uid) != 1) {
                            dbP.insert("insert into user_info_count values(" + uid + ", 1, 0, 0)");
                        }
                        if (DFT_ID != null)
                            dbP.delete("delete from draft where dftid = " + DFT_ID);
                        writeHandler.post(sendRunnable);
                    } else
                        writeHandler.post(serverErrorRunnable);
                }
                dbP.closeConn();
                waitDialog.dismiss();
            }
        }).start();
    }

    private void connectToSave(final int uid, final String content) {
        waitDialog = ProgressDialog.show(WriteActivity.this, "请稍后", "正在请数据库君吃饭……");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBProcessor dbP = new DBProcessor();
                if (dbP.getConn(opts_o) == null) {
                    writeHandler.post(timeOutRunnable);
                } else {
                    int res = dbP.insert(
                            "insert into draft(uid, savetime, content) values(" +
                                    uid + ", NOW(), '" + content + "')"
                    );
                    if (res == 1)
                        writeHandler.post(saveRunnable);
                    else
                        writeHandler.post(serverErrorRunnable);
                }
                dbP.closeConn();
                waitDialog.dismiss();
            }
        }).start();
    }

    private TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            Log.d("TEST", "beforeTC:\n" + "start: " + start + "\nafter:" + after + "\ncount" + count);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            Log.d("TEST", "onTC:\n" + "start: " + start + "\nbefore:" + before + "\ncount" + count);
            Editable editable = writeEdt.getText();
            int len = editable.length();
            restWordTv.setText("还能输入" + (WORD_LIMIT - len) + "个字");
            if (len > WORD_LIMIT) {
                int selectEndIndex = Selection.getSelectionEnd(editable); // getSelectionEnd获取光标结束的索引值
                String str = editable.toString(); // 旧字符串
                String newStr = str.substring(0, WORD_LIMIT); // 截取新字符串
                writeEdt.setText(newStr);
                editable = writeEdt.getText();
                int newLength = editable.length(); // 新字符串长度
                if (selectEndIndex > newLength) { // 如果光标结束的索引值超过新字符串长度
                    selectEndIndex = editable.length();
//                    Toast.makeText(WriteActivity.this, "最多只能输入" + selectEndIndex + "个字哦！", Toast.LENGTH_SHORT).show();
                }
                Selection.setSelection(editable, selectEndIndex); // 设置新光标所在的位置
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            ((GlobalApp) getApplication()).setHomeActiveUpdated(true);
            isSent = true;
            Toast.makeText(WriteActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    };

    private Runnable saveRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(WriteActivity.this, "成功保存到草稿箱啦", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    };

    private Runnable serverErrorRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(WriteActivity.this, "可能有非法字符，导致服务器傻掉，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(WriteActivity.this, "连接超时啦，请重试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable compressErrorRunnable = new Runnable() {
        @Override
        public void run() {
            isImgUploaded = false;
            Toast.makeText(WriteActivity.this, "图片处理出错，请重发试试", Toast.LENGTH_SHORT).show();
        }
    };

    private Runnable imgUploadRunnable = new Runnable() {
        @Override
        public void run() {
            AsyncHttpClient httpClient = new AsyncHttpClient();
            httpClient.setTimeout(16 * 1000);
            httpClient.post(IMAGES_UPLOAD_URL, params, new TextHttpResponseHandler() {
                @Override
                public void onStart() {
                    waitDialog = ProgressDialog.show(WriteActivity.this, "请稍后", "正在上传图片……");
                    super.onStart();
                }

                @Override
                public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                    waitDialog.dismiss();
                    isImgUploaded = false;
                    Toast.makeText(WriteActivity.this, "图片传输失败，请重试发送", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(int i, Header[] headers, String s) {
                    waitDialog.dismiss();
                    if (s.contains("Success")) {
                        isImgUploaded = true;
                        imgInfo = (s.split(":"))[1] + "_" + TIME_POINT + "_" + imagesPath.size();
                        sendText(UID, CONTENT, imgInfo);
                    } else if (s.contains("Failure")) {
                        isImgUploaded = false;
                        imgInfo = (s.split(":"))[1] + "_" + TIME_POINT + "_" + imagesPath.size();
                        Toast.makeText(WriteActivity.this, "图片上传失败，请重试发送", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    @Override
    public void onBackPressed() {
        if (!isSent) {
            delImg(UID, imgInfo);
        }
        super.onBackPressed();
    }

    private void delImg(String uid, String imgInfo) {
        if (uid != null && imgInfo != null) {
            AsyncHttpClient httpClient = new AsyncHttpClient();
            httpClient.setTimeout(16 * 1000);
            RequestParams rPs = new RequestParams();
            rPs.put("uid", uid);
            rPs.put("del_img", imgInfo);
            httpClient.post(IMAGES_DEL_URL, rPs, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_write, menu);
        MenuItem menuItem = menu.findItem(R.id.action_send);
        menuItem.setOnMenuItemClickListener(new NoDoubleMenuItemClickListener() {
            @Override
            protected void onNoDoubleClick(MenuItem item) {
                String writeContent = writeEdt.getText().toString();
                writeContent = StringUtils.zipBlank(writeContent);
                if (!StringUtils.replaceBlank(writeContent).equals(""))
                    send(UID, writeContent);
                else
                    Toast.makeText(WriteActivity.this, "不能什么都不说哦", Toast.LENGTH_SHORT).show();
            }
        });

        menu.findItem(R.id.action_save).setOnMenuItemClickListener(new NoDoubleMenuItemClickListener() {
            @Override
            protected void onNoDoubleClick(MenuItem item) {
                String writeContent = writeEdt.getText().toString();
                writeContent = StringUtils.zipBlank(writeContent);
                if (!StringUtils.replaceBlank(writeContent).equals(""))
                    save(Integer.parseInt(UID), writeContent);
                else
                    Toast.makeText(WriteActivity.this, "不能什么都不说哦", Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageSelector.REQUEST_SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> paths = data.getStringArrayListExtra(ImageSelector.SELECTED_RESULT);
                imagePos = data.getIntegerArrayListExtra(ImageSelector.SELECTED_POS);
                imagesPath.clear();
                for (int i = 0; i < paths.size(); i++)
                    imagesPath.add(paths.get(i));
                listViewAdapter.notifyDataSetChanged();
            }
        }
    }

    private void removeSelectedImg(final int position) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("紧张的提示框").setMessage("要移除这张图片吗？").setCancelable(true)
                .setPositiveButton("好哒", new NoDouleDialogClickListener() {
                    @Override
                    protected void onNoDoubleClick(DialogInterface dialog, int which) {
                        imagesPath.remove(imagesPath.get(position));
                        imagePos.remove(imagePos.get(position));
                        listViewAdapter.notifyDataSetChanged();
                    }
                }).setNegativeButton("再想想", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private byte[] fileToBytes(File file) {
        FileChannel fC = null;
        try {
            fC = new RandomAccessFile(file, "r").getChannel();
            MappedByteBuffer buffer = fC.map(FileChannel.MapMode.READ_ONLY, 0,
                    fC.size()).load();
            byte[] bytes = new byte[(int) fC.size()];
            if (buffer.remaining() > 0) {
                buffer.get(bytes, 0, buffer.remaining());
            }
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fC != null)
                    fC.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private int[] getImgWH(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        return new int[]{options.outWidth, options.outHeight};
    }
}
