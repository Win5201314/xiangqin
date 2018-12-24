package com.zsl.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;

import com.zsl.Util.UtilTools;
import com.zsl.adapter.LikeMeAdapter;
import com.zsl.bean.Detail;
import com.zsl.bean.LikeBean;
import com.zsl.xiangqin.R;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class LikeOtherActivity extends BaseActivity implements LikeMeAdapter.OnItemClickListener {

    private SwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView recyclerView;
    private LikeMeAdapter likeMeAdapter;
    private List<Detail> details = new ArrayList<>();
    private static int lastVisibleItem = 0;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:{
                    mSwipeRefreshWidget.setRefreshing(false);
                    likeMeAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);

        mSwipeRefreshWidget = findViewById(R.id.swipe_refresh_widget);

        mSwipeRefreshWidget.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        mSwipeRefreshWidget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onrefresh();
            }

        });

        // 这句话是为了，第一次进入页面的时候显示加载进度条
        mSwipeRefreshWidget.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));

        recyclerView = findViewById(R.id.rv);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper. VERTICAL);
        //设置Adapter
        likeMeAdapter = new LikeMeAdapter(details, this);
        recyclerView.setAdapter(likeMeAdapter);
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator( new DefaultItemAnimator());
        //设置列表点击事件
        likeMeAdapter.setOnItemClickListener(this);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == likeMeAdapter.getItemCount()) {
                    onrefresh();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }

        });
        onrefresh();
    }

    //刷新获取后台数据
    private void onrefresh() {
        mSwipeRefreshWidget.setRefreshing(true);
        // 此处在现实项目中，请换成网络请求数据代码，sendRequest .....
        new Thread(new Runnable() {
            @Override
            public void run() {
                //先将本人信息查出来
                BmobQuery<LikeBean> query = new BmobQuery<>();
                //查询playerName叫“比目”的数据
                query.addWhereEqualTo("phone", UtilTools.loginedPhone());
                //返回1条数据，如果不加上这条语句，默认返回10条数据
                query.setLimit(1);
                //最新数据优先排出
                query.order("-createdAt");
                //执行查询方法
                query.findObjects(new FindListener<LikeBean>() {
                    @Override
                    public void done(List<LikeBean> object, BmobException e) {
                        if (e == null) {
                            if (object.size() <= 0) {
                                handler.sendEmptyMessage(0);
                                //ToastUtil.normalShow(context, "不存在,可能已被移除!", true);
                                return;
                            }
                            LikeBean likeBean = object.get(0);
                            String likeOther = likeBean.getLikeOther();
                            String[] lm = likeOther.split(",");
                            for (String s : lm) { findDetail(s); }
                            handler.sendEmptyMessage(0);
                        }
                    }
                });
            }
        }).start();
    }

    private void findDetail(String phone) {
        //先将本人信息查出来
        BmobQuery<Detail> query = new BmobQuery<>();
        //查询playerName叫“比目”的数据
        query.addWhereEqualTo("phone", phone);
        //返回1条数据，如果不加上这条语句，默认返回10条数据
        query.setLimit(1);
        //最新数据优先排出
        query.order("-createdAt");
        //执行查询方法
        query.findObjects(new FindListener<Detail>() {
            @Override
            public void done(List<Detail> object, BmobException e) {
                if (e == null) {
                    if (object.size() <= 0) { return; }
                    Detail detail = object.get(0);
                    if (detail != null && isNeedAdd(detail)) details.add(detail);
                }
            }
        });
    }

    public boolean isNeedAdd(Detail d) {
        String phoneNumber = d.getPhone();
        boolean flag = false;
        for (Detail detail : details) {
            flag = phoneNumber.equals(detail.getPhone());
            if (flag) return false;
        }
        return true;
    }

    @Override
    public void onClick(int position) {
        if (details.size() > position) {
            Detail detail = details.get(position);
            if (detail != null) {
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra("detail", detail);
                intent.putExtra("like", "like");
                startActivity(intent);
            }
        }
    }

}
