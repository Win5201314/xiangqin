package com.zsl.room.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zsl.room.R;
import com.zsl.room.adapter.Hzadapter;
import com.zsl.room.bean.HomeBean;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class HeZuFragment extends Fragment implements Hzadapter.OnItemClickListener{

    private SwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView recyclerView;
    private List<HomeBean> homeBeans = new ArrayList<>();
    private Hzadapter hzadapter;
    private static int lastVisibleItem = 0;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:{
                    mSwipeRefreshWidget.setRefreshing(false);
                    hzadapter.notifyDataSetChanged();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_nav, container, false);
        mSwipeRefreshWidget = v.findViewById(R.id.swipe_refresh_widget);

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

        recyclerView = v.findViewById(R.id.rv);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper. VERTICAL);
        //设置Adapter
        hzadapter = new Hzadapter(homeBeans, getActivity());
        recyclerView.setAdapter(hzadapter);
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator( new DefaultItemAnimator());
        //设置列表点击事件
        hzadapter.setOnItemClickListener(this);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == hzadapter.getItemCount()) {
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
        return v;
    }

    private void onrefresh() {
        mSwipeRefreshWidget.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                BmobQuery<HomeBean> query = new BmobQuery<>();
                //查询playerName叫“比目”的数据
                //query.addWhereEqualTo("type", type);
                //返回50条数据，如果不加上这条语句，默认返回10条数据
                query.setLimit(50);
                //最新数据优先排出
                query.order("-createdAt");
                //执行查询方法
                query.findObjects(new FindListener<HomeBean>() {
                    @Override
                    public void done(List<HomeBean> object, BmobException e) {
                        if (e == null) {
                            if (object.size() <= 0) {
                                handler.sendEmptyMessage(1);
                                return;
                            }
                            homeBeans.addAll(object);
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

    }
}
