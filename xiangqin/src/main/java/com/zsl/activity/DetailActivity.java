package com.zsl.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.zsl.xiangqin.R;

import java.util.ArrayList;

public class DetailActivity extends BaseActivity implements View.OnClickListener {

    private AppCompatTextView message;
    private AppCompatImageView image1, image2, image3;
    private Context context;

    private ArrayList<String> urls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        context = this;
        StringBuffer sb = new StringBuffer();
        sb.append("名字:张胜利\n")
                .append("性别:男\n")
                .append("号码：13480901446\n")
                .append("出生年月：92.2\n")
                .append("有无婚史：无\n")
                .append("家乡：湖北咸宁\n")
                .append("现居地：深圳福田\n")
                .append("身高：169cm\n")
                .append("体重：120\n")
                .append("星座：水瓶座\n")
                .append("学历：本科\n")
                .append("职业工作：IT程序员\n")
                .append("平均月薪：12K+\n")
                .append("车房情况:咸宁有一套新房，目前没车，需要时再买\n")
                .append("自我个性描述：逗比，随性\n")
                .append("自我规划：目前还是努力工作\n")
                .append("另一半的要求：孝顺父母，独立，身高160-170\n")
                .append("邮箱地址：229589815@qq.com");
        String s = sb.toString();
        message = findViewById(R.id.message);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        message.setText(s);
        image1.setImageResource(R.mipmap.sc1);
        image2.setImageResource(R.mipmap.sc2);
        image3.setImageResource(R.mipmap.sc3);
        /*image1.setOnClickListener(this);
        image2.setOnClickListener(this);
        image3.setOnClickListener(this);
        urls = new ArrayList<>();*/
    }

    /**
     * 打开图片查看器
     *
     * @param position
     * @param urls2
     */
    protected void imageBrower(int position, ArrayList<String> urls2) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        // 图片url,为了演示这里使用常量，一般从数据库中或网络中获取
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urls2);
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image1:
                imageBrower(0, urls);
                break;
            case R.id.image2:
                imageBrower(1, urls);
                break;
            case R.id.image3:
                imageBrower(2, urls);
                break;
        }
    }
}
