package com.zsl.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.zsl.Util.UtilTools;
import com.zsl.adapter.NewsBeanAdapter;
import com.zsl.bean.NewsBean;
import com.zsl.xiangqin.R;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class NewsActivity extends BaseActivity implements NewsBeanAdapter.OnItemClickListener {

    private SwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView recyclerView;
    private List<NewsBean> newsBeans = new ArrayList<>();
    private NewsBeanAdapter newsBeanAdapter;
    private static int lastVisibleItem = 0;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:{
                    mSwipeRefreshWidget.setRefreshing(false);
                    newsBeanAdapter.notifyDataSetChanged();
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
        setContentView(R.layout.activity_news);
        if (UtilTools.isLogined() && UtilTools.isBoss()) {
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(NewsActivity.this, SendNewsActivity.class));
                }
            });
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
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置Adapter
        newsBeanAdapter = new NewsBeanAdapter(newsBeans, this);
        recyclerView.setAdapter(newsBeanAdapter);
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator( new DefaultItemAnimator());
        //设置列表点击事件
        newsBeanAdapter.setOnItemClickListener(this);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == newsBeanAdapter.getItemCount()) {
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
                BmobQuery<NewsBean> query = new BmobQuery<>();
                //查询playerName叫“比目”的数据
                //query.addWhereEqualTo("type", type);
                //返回50条数据，如果不加上这条语句，默认返回10条数据
                query.setLimit(50);
                //最新数据优先排出
                query.order("-createdAt");
                //执行查询方法
                query.findObjects(new FindListener<NewsBean>() {
                    @Override
                    public void done(List<NewsBean> object, BmobException e) {
                        if (e == null) {
                            if (object.size() <= 0) {
                                handler.sendEmptyMessage(1);
                                return;
                            }
                            if (newsBeans.size() < 50) {
                                List<NewsBean> newsBeans2 = new ArrayList<>();
                                for (NewsBean bean : object) {
                                    if (isNeedAddNews(bean.getData())) {
                                        newsBeans2.add(bean);
                                    }
                                }
                                newsBeans.addAll(newsBeans2);
                            } else {
                                newsBeans.addAll(object);
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

    @Override
    public void onClick(int position) {
        Log.d("TAG", newsBeans.size() + "");
        if (newsBeans.size() > position) {
            Intent intent = new Intent(this, ShowNewsActivity.class);
            intent.putExtra("news", newsBeans.get(position));
            startActivity(intent);
        }
    }

    private boolean isNeedAddNews(String data) {
        boolean flag = false;
        for (NewsBean bean : newsBeans) {
            flag = data.equals(bean.getData());
            if (flag) return false;
        }
        return true;
    }
}
