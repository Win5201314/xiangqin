package com.socialassistant_youyuelive.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.VolleyError;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.adapter.Msginfoadapter;
import com.socialassistant_youyuelive.commomentity.AboutMoney;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.Orders;
import com.socialassistant_youyuelive.commomentity.UserData;
import com.socialassistant_youyuelive.commomentity.UserID;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.ShowToast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ChatActivity extends BaseActivity implements View.OnClickListener {

    private TextView anchorName;
    private TextView sendMsg;
    private EditText msg;
    private ListView lvMsg;
    private Msginfoadapter adapter;
    private List<Orders> mlist;
    private Orders orders;
    //读取主播(图灵)回复次数
    public static int anchorTalkCurrent = 0;
    //图灵回复赋值
    private String anchorMsg;
    private String text;
    private String url;
    private UserID userID;
    private Context context;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //onrefresh();
                    Orders neworders = new Orders();
                    neworders.setNickName(orders.getNickName());
                    neworders.setFaceUrl(orders.getFaceUrl());
                    neworders.setMessage(text);
                    mlist.add(neworders);
                    adapter.notifyDataSetChanged();
                    break;
                case 2:
                    mlist.clear();
                    Intent intent = new Intent(ChatActivity.this,AboutAnchordActivity.class);
                    intent.putExtra("user_info",userID);
                    startActivity(intent);
                    break;
                case 3:{
                    //ShowToast.normalShow(context,"主播可能在忙,请于他(她)进行视频通话吧!",false);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initToolbar(R.id.toolbar, R.id.anchorName, "么么哒");
        context = this;
        init();
        /*//判断是否是会员
        if (ConstString.isVIP()) showAlertDialog("", "快去找主播聊天吧!", 0);*/
    }

    private void init() {
        mlist = new ArrayList<>();
        adapter = new Msginfoadapter(this,mlist);
        sendMsg = (TextView) findViewById(R.id.sendMsg);
        msg = (EditText) findViewById(R.id.msg);
        lvMsg = (ListView) findViewById(R.id.chat_lv);
        lvMsg.setAdapter(adapter);
        sendMsg.setOnClickListener(this);
        msg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                lvMsg.setSelection(lvMsg.getCount() - 1);
            }
        });
        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lvMsg.setSelection(lvMsg.getCount() - 1);
            }
        });
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        Toolbar toolbar = (Toolbar) findViewById(id);
        //toolbar.setTitle("");
        anchorName = (TextView) findViewById(titleId);
        anchorName.setText(titleString);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
    }

    @Override
    public void onClick(View v) {
        Orders orders1 = new Orders();
        //用户发送的消息
        final String usermsg = msg.getText().toString().trim();
        ConstString.updateUserData();
        //用户头像获取
        JSONObject jo = JSON.parseObject(ConstString.user);
        if(jo == null) return;
        String headImgUrl = jo.getString("headImgUrl");
        //点击事件
        switch (v.getId()){
            //点击发送
            case R.id.sendMsg:{
                if(usermsg.equals("")) return;
                orders1.setUserfaceUrl(headImgUrl);
                orders1.setUsertext(usermsg);
                AboutMoney aboutMoney = new AboutMoney();
                aboutMoney.setUsermessage(usermsg);
                aboutMoney.setUserfaceUrl(headImgUrl);
                aboutMoney.setType("0");
                aboutMoney.setReadsum("1");
                aboutMoney.setNickName(orders.getNickName());
                aboutMoney.setFaceUrl(orders.getFaceUrl());
                aboutMoney.setAnchorId(orders.getAmchorId());
                long currentTime = System.currentTimeMillis();
                String time = stampToDate(currentTime);
                aboutMoney.setTime(time);
                aboutMoney.setTalksum("0");
                aboutMoney.save();
                mlist.add(orders1);
                adapter.notifyDataSetChanged();
                //如果出现了大于等于1次的主播回复则取消图灵机器人回复
                if(queryAnchorTalkCurrent()) TULINGHUIFU(usermsg);
                break;
            }
        }
        //输入完毕,将listview显示最后一个item
        lvMsg.smoothScrollToPosition(lvMsg.getCount() - 1);
        msg.setText("");
    }
    /*
     *  将时间戳转换为时间
     */
    public String stampToDate(long timeMillis){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }
    private boolean queryAnchorTalkCurrent(){
        Cursor cursor = DataSupport.findBySQL("select * from AboutMoney where talksum = 1 and nickname = '"+ orders.getNickName() + "'");
        while (cursor.moveToNext()) anchorTalkCurrent ++;
        cursor.close();
        Log.d("anchorTalk",anchorTalkCurrent + "");
        if(anchorTalkCurrent >= 1){
            if(!ConstString.isVIP()){
                //不是Vip删除当前用户发送的消息,不能显示出来
                if(mlist != null && mlist.size() > 0){
                    mlist.remove(mlist.size() - 1);
                    adapter.notifyDataSetChanged();
                }
                showDialog();
            }else {
                Orders anchorBusy = new Orders();
                anchorBusy.setUsertext("主播如果未回复,请于他(她)进行视频通话吧!");
                mlist.add(anchorBusy);
                adapter.notifyDataSetChanged();
                AboutMoney aboutMoney = new AboutMoney();
                aboutMoney.setUsermessage("主播如果未回复,请于他(她)进行视频通话吧!");
                aboutMoney.setType("0");
                aboutMoney.setReadsum("1");
                aboutMoney.setNickName(orders.getNickName());
                aboutMoney.setFaceUrl(orders.getFaceUrl());
                aboutMoney.setAnchorId(orders.getAmchorId());
                long currentTime = System.currentTimeMillis();
                String time = stampToDate(currentTime);
                aboutMoney.setTime(time);
                aboutMoney.setTalksum("0");
                aboutMoney.save();
            }
            anchorTalkCurrent = 0;
            return false;
        }else {
            anchorTalkCurrent = 0;
            return true;
        }
    }

    private void showDialog() {
        new com.socialassistant_youyuelive.commomentity.AlertDialog(context)
                .builder()
                .setMsg("温馨提示:成为会员才能与主播继续聊天\n¥45元即可成为会员(VIP特权:通话8折)")
                .setCancelable(false)
                .setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showgetMoneyDialog();
                    }
                }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg.setText("");
            }
        }).show();
    }

    private void showgetMoneyDialog() {
        new com.socialassistant_youyuelive.commomentity.AlertDialog(context)
                .builder()
                .setMsg("确定要消费¥45元成为永久会员")
                .setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*Intent intent = new Intent(ChatActivity.this,VIPActivity.class);
                        startActivity(intent);*/
                        //直接扣费
                        gotoVip();
                    }
                }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg.setText("");
            }
        }).show();
    }
    private Map<String,String> params = new HashMap<>();
    private void gotoVip() {
        ConstString.updateUserData();
        params.put("userId",ConstString.userId);
        params.put("time",String.valueOf(System.currentTimeMillis()));
        HttpUtil.GoToVip(new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                JSONObject jsonObject = JSON.parseObject(s);
                if (jsonObject == null) {
                    ShowToast.show(context, R.string.access_fail, true);
                    return;
                }
                //status为false时未知原因 支付失败
                //status 为true时 values为0表示余额不足 支付失败   为1时支付成功
                boolean state = jsonObject.getBooleanValue("status");
                if (state) {
                    int values = jsonObject.getIntValue("values");
                    switch (values) {
                        case 0:
                            ShowToast.normalShow(context,"余额不足，支付失败,请及时充值!",false);
                            Intent intent  = new Intent(context,VoucherActivity.class);
                            startActivity(intent);
                            break;
                        case 1:
                            ShowToast.normalShow(context,"支付成功,您已经升级成为会员了!",false);
                            ConstString.updateUserData();
                            JSONObject json = JSON.parseObject(ConstString.user);
                            if (json == null) return;
                            //member 0代表费会员， 1代表会员
                            if (json.containsKey("member")) {
                                json.put("member", 1);
                                ConstString.user = json.toString();
                                UserData userData = DataSupport.findFirst(UserData.class);
                                if (userData != null) {
                                    userData.setUserData(ConstString.user);
                                    userData.save();
                                }
                            }
                            break;
                    }
                } else {
                    ShowToast.normalShow(context,"支付失败,请稍后重试!",false);
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        },params);
    }

    //调用图灵接口进行回复----然后作为对象放到listview上,作为回复
    private void TULINGHUIFU(final String usermsg) {
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    anchorMsg = TULING_REPLY(ConstString.TULING_KEY,usermsg);
                    readJSON(anchorMsg);//解析JSON字符串，获得想要的数据
                    if(text.contains("图灵机器人")){
                        return;
                    }else if (text.contains("机器")){
                        return;
                    }else if (text.contains("图灵")){
                        return;
                    }
                    AboutMoney aboutMoney = new AboutMoney();
                    aboutMoney.setMessage(text);
                    aboutMoney.setType("0");
                    aboutMoney.setReadsum("1");
                    aboutMoney.setNickName(orders.getNickName());
                    aboutMoney.setFaceUrl(orders.getFaceUrl());
                    aboutMoney.setAnchorId(orders.getAmchorId());
                    long timenum = System.currentTimeMillis();
                    String time1 = stampToDate(timenum);
                    aboutMoney.setTime(time1);
                    aboutMoney.setTalksum("1");
                    aboutMoney.save();
                }catch (IOException e){
                    e.printStackTrace();
                    Log.e("IOException", e.toString());
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(1);
            }
        }.start();
    }

    private void readJSON(String anchorMsg) {
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(anchorMsg);
            text = jsonObject.getString("text");
            Log.i("text", text);
            url = jsonObject.getString("url");
        } catch (JSONException e) {
            url = null;
            e.printStackTrace();
        }
    }

    public static String TULING_REPLY(String[] key,String usermsg) throws IOException{
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"key\":\"" + key[0] + "\" ,\"info\":\""+ usermsg +"\"}");
        Request request = new Request.Builder()
                .url(ConstString.TULING_URL)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException();
        return response.body().string();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onrefresh();
    }

    private void onrefresh() {
        if(mlist != null){
            mlist.clear();
        }else {
            mlist = new ArrayList<>();
        }
        Intent intent = getIntent();
        orders = (Orders) intent.getSerializableExtra("orders");
        Cursor cursor = DataSupport.findBySQL("select *  from AboutMoney where type = 0 and nickname = '" + orders.getNickName() + "'");
        if (cursor.getCount() != 0) {
            if(cursor.moveToFirst()){
                do {
                    Orders orders2 = new Orders();
                    if (cursor.getString(cursor.getColumnIndex("message")) == null) {
                        orders2.setUsertext(cursor.getString(cursor.getColumnIndex("usermessage")));
                        orders2.setUserfaceUrl(cursor.getString(cursor.getColumnIndex("userfaceurl")));
                    } else {
                        orders2.setMessage(cursor.getString(cursor.getColumnIndex("message")));
                        orders2.setFaceUrl(cursor.getString(cursor.getColumnIndex("faceurl")));
                        orders2.setNickName(cursor.getString(cursor.getColumnIndex("nickname")));
                    }
                    orders2.setAmchorId(cursor.getString(cursor.getColumnIndex("anchorid")));
                    mlist.add(orders2);
                }while (cursor.moveToNext());
            }
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        lvMsg.setSelection(adapter.getCount());
        anchorName.setText(orders.getNickName());
    }

    private void showAlertDialog(String title, String msg, final int type) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(true)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (type) {
                            case 0:
                                mlist.clear();
                                if (orders != null) {
                                    HttpUtil.getAnchorData(new com.android.volley.Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String s) {
                                            if(s != null && !s.equals("")){
                                                JSONObject jsonobject = JSON.parseObject(s);
                                                if(jsonobject == null) return;
                                                String state = jsonobject.getString("state");
                                                if(state != null && state.equals("ok")){
                                                    loadUserId(jsonobject,state);
                                                }
                                            }
                                        }
                                    }, new com.android.volley.Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError volleyError) {
                                        }
                                    }, orders.getAmchorId());
                                }
                                break;
                            case 1:
                                startActivity(new Intent(context, VIPActivity.class));
                                finish();
                                break;
                        }
                    }
                }).show();
    }

    private void loadUserId(JSONObject jsonobject, String state) {
        String  userjson = jsonobject.getString("anchorinfo");
        JSONArray jsonarray = JSON.parseArray(userjson);
        for (int i = 0;i < jsonarray.size();i++){
            JSONObject jo = JSON.parseObject(jsonarray.get(i).toString());
            UserID userID = new UserID();
            //获取5个头像,已逗号分隔
            userID.setAlbum(jo.getString("album"));
            //主播头像
            userID.setFace_url(jo.getString("headImgUrl"));
            //主播昵称
            userID.setNickName(jo.getString("nickName"));
            //主播签名
            userID.setSignature(jo.getString("signature"));
            //主播点赞数量
            userID.setFriendsAccount(jo.getIntValue("friendsAccount"));
            //主播城市
            userID.setCity(jo.getString("city"));
            //主播省份
            userID.setProvince(jo.getString("province"));
            //主播ID
            userID.setAnchorId(jo.getIntValue("anchorId"));
            //主播手机号
            userID.setMobile(jo.getString("mobile"));
            //主播tokenID
            userID.setTokenId(jo.getString("tokenId"));
            //主播的三个标签,已逗号形式分开
            userID.setLabels(jo.getString("labels"));
            //获取语音URL
            userID.setVoiceUrl(jo.getString("voiceUrl"));
            //获取视频URL
            userID.setHeadVedio(jo.getString("headVedio"));
            if(jo.getBooleanValue("sex")){
                userID.setMan(true);
            }else {
                userID.setMan(false);
            }
            //判断是否正在直播
            if (jo.getIntValue("chat_status") == 0) {
                userID.setLive(true);
            }
            context.startActivity(new Intent(context,AboutAnchordActivity.class).putExtra("user_info",userID));
        }
    }

}
