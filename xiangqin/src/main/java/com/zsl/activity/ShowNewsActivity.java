package com.zsl.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.zsl.Util.ToastUtil;
import com.zsl.Util.UtilTools;
import com.zsl.bean.NewsBean;
import com.zsl.xiangqin.R;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class ShowNewsActivity extends BaseActivity {

    private AppCompatTextView news;
    private AppCompatButton delete;
    private NewsBean newsBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsshow);
        news = findViewById(R.id.news);
        delete = findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteNews();
            }
        });
        if (UtilTools.isBoss()) {
            delete.setVisibility(View.VISIBLE);
        }
        Intent intent = getIntent();
        newsBean = (NewsBean) intent.getSerializableExtra("news");
        news.setText(newsBean.getNews());
        this.setTitle(newsBean.getData());
    }

    private void deleteNews() {
        newsBean.delete(new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if (e == null) {
                    ToastUtil.normalShow(ShowNewsActivity.this, "删除成功!", true);
                } else {
                    ToastUtil.normalShow(ShowNewsActivity.this, "删除失败!", true);
                }
            }

        });
    }
}
