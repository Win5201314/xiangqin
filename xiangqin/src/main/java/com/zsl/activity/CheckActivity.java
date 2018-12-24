package com.zsl.activity;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.zsl.adapter.CheckAdapter;
import com.zsl.bean.BeforeDetail;
import com.zsl.xiangqin.R;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class CheckActivity extends BaseActivity implements CheckAdapter.OnItemClickListener {

    private SwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView recyclerView;
    private List<BeforeDetail> details = new ArrayList<>();
    private CheckAdapter checkAdapter;
    private static int lastVisibleItem = 0;

    private Context context;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:{
                    mSwipeRefreshWidget.setRefreshing(false);
                    checkAdapter.notifyDataSetChanged();
                    break;
                }
                case 1: {
                    mSwipeRefreshWidget.setRefreshing(false);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        context = this;
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
        checkAdapter = new CheckAdapter(details, this);
        recyclerView.setAdapter(checkAdapter);
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator( new DefaultItemAnimator());
        //设置列表点击事件
        checkAdapter.setOnItemClickListener(this);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == checkAdapter.getItemCount()) {
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                BmobQuery<BeforeDetail> query = new BmobQuery<>();
                //查询playerName叫“比目”的数据
                //query.addWhereEqualTo("type", type);
                //返回50条数据，如果不加上这条语句，默认返回10条数据
                query.setLimit(50);
                //最新数据优先排出
                query.order("-createdAt");
                //执行查询方法
                query.findObjects(new FindListener<BeforeDetail>() {
                    @Override
                    public void done(List<BeforeDetail> object, BmobException e) {
                        if (e == null) {
                            if (object.size() <= 0) {
                                handler.sendEmptyMessage(1);
                                return;
                            }
                            if (details.size() < 50) {
                                List<BeforeDetail> details2 = new ArrayList<>();
                                for (BeforeDetail detail : object) {
                                    if (isNeedAdd(detail)) details2.add(detail);
                                }
                                details.addAll(details2);
                            } else {
                                details.addAll(object);
                            }
                            handler.sendEmptyMessage(0);
                        } else {
                            handler.sendEmptyMessage(1);
                        }
                    }
                });
            }
        }).start();
    }

    public boolean isNeedAdd(BeforeDetail d) {
        String phoneNumber = d.getPhone();
        boolean flag = false;
        for (BeforeDetail detail : details) {
            flag = phoneNumber.equals(detail.getPhone());
            if (flag) return false;
        }
        return true;
    }

    @Override
    public void onClick(int position) {
        if (details.size() > position) {
            if (details.get(position) == null) return;
            Intent intent = new Intent(this, BeforeDetailActivity.class);
            intent.putExtra("detail", details.get(position));
            startActivity(intent);
        }
    }
}
