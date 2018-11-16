package com.socialassistant_youyuelive.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.activity.ChatActivity;
import com.socialassistant_youyuelive.activity.SystemMsgActivity;
import com.socialassistant_youyuelive.adapter.MsgMessageAdapter;
import com.socialassistant_youyuelive.commomentity.AboutMoney;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.Orders;
import com.socialassistant_youyuelive.db.MyDataBaseHelper;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListFragmentMessage extends BaseFragment implements  AdapterView.OnItemClickListener, View.OnClickListener {
    // 标志位，标志已经初始化完成。
    private boolean isMsgViewOver;
    //当前listView
    public static ListView mlistView;
    //适配器
    public static MsgMessageAdapter adapter;
    //从网络中加载数据
    public static List<Orders> msgList = new ArrayList<>();
    //头布局
    public static View headerview;
    //头布局1中的小红点
    public static RelativeLayout repointlayout;
    //下拉刷新布局
    public static SwipeRefreshLayout mSwiperefreshlayout;
    //public static final String ACTION = "MSG_JPUSH_HC";
    public MyDataBaseHelper mB;
    public SQLiteDatabase db;
    //更新系统消息---小红点---未读消息
    public static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                //接收到系统通知时 通知小圆点显示
                case 1 :{
                    if(null != repointlayout){
                        repointlayout.setVisibility(View.VISIBLE);
                    }
                    break;
                }
            }
        }
    };
    public Handler pushhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                if(!mlistView.isShown()){
                    mlistView.setVisibility(View.VISIBLE);
                }
                adapter.addAll(msgList,true);
            }
        }
    };

    public ListFragmentMessage() {
        // Required empty public constructor
    }
    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message,container,false);
        isMsgViewOver = true;
        init(view);
        return view;
    }
    @Override
    public void initData() {

    }

    @Override
    public void onDetach() {
        super.onDetach();
//        getActivity().unregisterReceiver(myReceiver);
        //清空线程池中的数据
        if(singleTaskPool != null){
            singleTaskPool.shutdownNow();
            singleTaskPool = null;
        }
    }

    private void init(View view) {
        //Log.d("fragmentMessage","isVisible" + isVisible);
        //只要为true 在这个地方会报异常----会返回
        if(!isMsgViewOver || isVisible) {
            return;
        }
        mlistView = (ListView) view.findViewById(R.id.msg_lv);
        //加载头布局
        headerview = LayoutInflater.from(getActivity()).inflate(R.layout.message_header_view,null);
        repointlayout = (RelativeLayout) headerview.findViewById(R.id.msg_rad_circle);
        mSwiperefreshlayout = (SwipeRefreshLayout) view.findViewById(R.id.msg_swiperefreshlayout);
        mSwiperefreshlayout.setColorSchemeResources(R.color.fense);
        mlistView.addHeaderView(headerview);
        adapter = new MsgMessageAdapter(getActivity());
        mlistView.setAdapter(adapter);
        headerview.setOnClickListener(this);
        mlistView.setOnItemClickListener(this);
        mSwiperefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onrefresh();
                mSwiperefreshlayout.setRefreshing(false);
            }
        });
        /*mlistView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState){
                    //当不滚动的时候
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        //判断是否是最底部
                        if(view.getLastVisiblePosition()==(view.getCount())-1){
                            onrefresh();
                        }
                        break;
                    //清除焦点---可能不行
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:{
                        View currentFocus = getActivity().getCurrentFocus();
                        if (currentFocus != null) {
                            currentFocus.clearFocus();
                        }
                        break;
                    }
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });*/
    }
    //每个Item点击项
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Orders orders = msgList.get(position - 1);
        //db = MyDataBaseHelper.getInstance(getActivity()).getWritableDatabase();
        //更新 readsum 要根据某个条件进行修改
        AboutMoney aboutMoney = new AboutMoney();
        aboutMoney.setNickName(orders.getNickName());
        aboutMoney.setReadsum("1");
        aboutMoney.updateAll("nickname = ?",orders.getNickName());
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("orders",orders);
        startActivity(intent);
    }
    @Override
    public void onResume() {
        super.onResume();
        /*if(!msgList.isEmpty()){
            return;
        }*/
        //当fragment加入activity后 再更新数据
        if(isAdded()){
            onrefresh();
        }
    }

    /**
     * 线程池单例
     */
    public static ExecutorService singleTaskPool;
    public static ExecutorService getSingleThreadPool(){
        if(singleTaskPool == null){
            synchronized (ListFragmentMessage.class) {
                if(singleTaskPool == null){
                    singleTaskPool = Executors.newSingleThreadExecutor();
                }
            }
        }
        return singleTaskPool;
    }
    private void onrefresh() {
         /*getSingleThreadPool().execute(new Runnable() {
             @Override
             public void run() {
                 if(msgList != null){
                     msgList.clear();
                 }
                 mB = MyDataBaseHelper.getInstance(getActivity());
                 db = mB.getWritableDatabase();
                 Cursor cursor = db.rawQuery("select * , count(message) as count from notice where type = 0 group by nickName",null);
                 Cursor cursor1 = null;
                 if(cursor.getCount() != 0){
                     if(cursor.getColumnCount() != 0){
                         while (cursor.moveToNext()) {
                             Orders orders = new Orders();
                             orders.setNickName(cursor.getString(cursor.getColumnIndex("nickName")));
                             orders.setFaceUrl(cursor.getString(cursor.getColumnIndex("faceUrl")));
                             orders.setAmchorId(cursor.getString(cursor.getColumnIndex("id")));
                             orders.setRecordId(cursor.getString(cursor.getColumnIndex("recordId")));
                             orders.setTime(cursor.getString(cursor.getColumnIndex("time")));
                             if(cursor.getString(cursor.getColumnIndex("message")) == null){
                                 orders.setUsertext(cursor.getString(cursor.getColumnIndex("usermessage")));
                             }else {
                                 orders.setMessage(cursor.getString(cursor.getColumnIndex("message")));
                             }
                             cursor1 = db.rawQuery("select *  from notice where type = 0 and readflag = 0 and nickName = '" + orders.getNickName() +"'",null);
                             orders.setUnread(cursor1.getCount());
                             msgList.add(orders);
                         }
                     }
                 }else {
                     cursor.close();
                     return;
                 }
                 cursor.close();
                 if(cursor1 != null) cursor1.close();
                 pushhandler.sendEmptyMessage(1);
             }
         });*/
        getSingleThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if(!msgList.isEmpty()){
                    msgList.clear();
                }
                Cursor cursor = DataSupport.findBySQL("select * , count(message) as count from AboutMoney where type = 0 group by nickname");
                Cursor cursor1 = null;
                if(cursor != null && cursor.getCount() != 0){
                    if(cursor.getColumnCount() != 0){
                        if (cursor.moveToFirst()) {
                            do{
                                Orders orders = new Orders();
                                orders.setNickName(cursor.getString(cursor.getColumnIndex("nickname")));
                                orders.setFaceUrl(cursor.getString(cursor.getColumnIndex("faceurl")));
                                orders.setAmchorId(cursor.getString(cursor.getColumnIndex("anchorid")));
                                orders.setRecordId(cursor.getString(cursor.getColumnIndex("recordid")));
                                orders.setTime(cursor.getString(cursor.getColumnIndex("time")));
                                if(cursor.getString(cursor.getColumnIndex("message")) == null){
                                    orders.setUsertext(cursor.getString(cursor.getColumnIndex("usermessage")));
                                }else {
                                    orders.setMessage(cursor.getString(cursor.getColumnIndex("message")));
                                }
                                cursor1 = DataSupport.findBySQL("select *  from AboutMoney where type = 0 and readsum = 0 and nickname = '" + orders.getNickName() +"'");
                                orders.setUnread(cursor1.getCount());
                                msgList.add(orders);
                            }while (cursor.moveToNext());
                        }
                    }
                }else {
                    return;
                }
                cursor.close();
                if(cursor1 != null) cursor1.close();
                pushhandler.sendEmptyMessage(1);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //头布局
            case R.id.msg_header:{
                //跳转到系统消息界面---里面包含各种消费明细
                startActivity(new Intent(getActivity(), SystemMsgActivity.class));
                ConstString.isNewMsg = false;
                repointlayout.setVisibility(View.INVISIBLE);
                break;
            }
        }
    }
}
