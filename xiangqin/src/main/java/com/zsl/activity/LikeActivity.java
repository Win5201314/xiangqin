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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import com.zsl.adapter.LikeMeAdapter;
import com.zsl.adapter.UserImageAdapter;
import com.zsl.bean.Detail;
import com.zsl.bean.UserImage;
import com.zsl.xiangqin.R;

import java.util.ArrayList;
import java.util.List;

//钟意我的
public class LikeActivity extends BaseActivity implements LikeMeAdapter.OnItemClickListener {

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

        for (int i = 0; i < 15; i++) {
            Detail detail = new Detail();
            detail.setName("张胜利");
            detail.setPhone("13480901446");
            detail.setBirthday("92.2");
            detail.setPath("湖北咸宁");
            detail.setResidence("深圳福田");
            detail.setCar("有房有车");
            detail.setEducation("本科");
            detail.setOccupation("IT程序员");
            detail.setSalary("12K+");
            details.add(detail);
        }

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
    }

    //刷新获取后台数据
    private void onrefresh() {
        mSwipeRefreshWidget.setRefreshing(true);
        // 此处在现实项目中，请换成网络请求数据代码，sendRequest .....
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 8; i++) {
                    Detail detail = new Detail();
                    detail.setName("张胜利");
                    detail.setPhone("13480901446");
                    detail.setBirthday("92.2");
                    detail.setPath("湖北咸宁");
                    detail.setResidence("深圳福田");
                    details.add(detail);
                }
                handler.sendEmptyMessageDelayed(0, 5000);
            }
        }).start();
    }

    @Override
    public void onClick(int position) {
        startActivity(new Intent(this, DetailActivity.class));
    }

}
