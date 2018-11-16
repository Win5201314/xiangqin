package com.zsl.itgod;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.zsl.Util.Logger;
import com.zsl.Util.ToastUtil;
import com.zsl.adapter.JobAdapter;
import com.zsl.bean.JobBean;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ShowActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private List<JobBean> jobBeans = new ArrayList<>();
    private JobAdapter jobAdapter;

    private int type = -1;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                //设置Adapter
                jobAdapter = new JobAdapter(jobBeans, ShowActivity.this);
                recyclerView.setAdapter(jobAdapter);
                jobAdapter.setOnItemClickListener(new JobAdapter.OnItemClickListener() {

                    @Override
                    public void onClick(int position) {
                        JobBean jobBean = jobBeans.get(position);
                        Intent intent1 = new Intent(ShowActivity.this, JobActivity.class);
                        intent1.putExtra("job", jobBean);
                        startActivity(intent1);
                    }
                });
                ToastUtil.normalShow(ShowActivity.this, "查询结束!", true);
            } else if (msg.what == 1) {
                ToastUtil.normalShow(ShowActivity.this, "查询失败!可能没有数据!", true);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show);
        final Intent intent = getIntent();
        if (intent != null) {
            type = intent.getIntExtra("type", -1);
        }
        initData();
        recyclerView = findViewById(R.id.rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper. VERTICAL);
        //设置Adapter
        jobAdapter = new JobAdapter(jobBeans, this);
        recyclerView.setAdapter(jobAdapter);
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator( new DefaultItemAnimator());
    }

    private void initData() {
        /*for (int i = 0; i < 10; i++) {
            JobBean jobBean = new JobBean("平安", type, "要求", "10K-15K", "深圳福田", false, "13480901446", true, "2018-9-13", "", "");
            jobBeans.add(jobBean);
        }*/

        new Thread(new Runnable() {
            @Override
            public void run() {
                BmobQuery<JobBean> query = new BmobQuery<>();
                //查询playerName叫“比目”的数据
                query.addWhereEqualTo("type", type);
                //返回50条数据，如果不加上这条语句，默认返回10条数据
                query.setLimit(500);
                //执行查询方法
                query.findObjects(new FindListener<JobBean>() {
                    @Override
                    public void done(List<JobBean> object, BmobException e) {
                        if(e == null) {
                            if (object.size() <= 0) return;
                            jobBeans.addAll(object);
                            handler.sendEmptyMessage(0);
                            Log.d("TAG", object.size() + "=========");
                        } else {
                            handler.sendEmptyMessage(1);
                            Log.d("TAG","失败："+e.getMessage()+","+e.getErrorCode());
                        }
                    }
                });
            }
        }).start();
    }
}
