package com.zsl.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.Button;

import com.squareup.picasso.Picasso;
import com.zsl.Util.ToastUtil;
import com.zsl.bean.Detail;
import com.zsl.bean.UserBean;
import com.zsl.xiangqin.R;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class MeSendActivity extends BaseActivity implements View.OnClickListener {

    private AppCompatTextView message;
    private AppCompatImageView image1, image2, image3;
    private Button delete;
    private Context context;
    private Detail detail = new Detail();

    private ArrayList<String> urls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesend);
        Intent intent = getIntent();
        detail = (Detail) intent.getSerializableExtra("detail");
        context = this;
        StringBuffer sb = new StringBuffer();
        sb.append("名字: "+ detail.getName() + "\n")
                .append("性别: " + detail.getSex() + "\n")
                .append("号码: " + detail.getPhone() + "\n")
                .append("出生年月: " + detail.getBirthday() + "\n")
                .append("有无婚史: " + detail.getMarry() + "\n")
                .append("家乡: " + detail.getPath() + "\n")
                .append("现居地: " + detail.getResidence() + "\n")
                .append("身高: " + detail.getHeight() + "\n")
                .append("体重: " + detail.getWeight() + "\n")
                .append("学历: " + detail.getEducation() + "\n")
                .append("职业工作: " + detail.getOccupation() + "\n")
                .append("平均月薪: " + detail.getSalary() + "\n")
                .append("车房情况: " + detail.getCar() + "\n")
                .append("另一半的要求: " + detail.getRequirement() + "\n");
        String s = sb.toString();
        message = findViewById(R.id.message);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image1.setOnClickListener(this);
        image2.setOnClickListener(this);
        image3.setOnClickListener(this);
        message.setText(s);
        urls = new ArrayList<>();
        String ss = detail.getImageUrl();
        urls.add(ss.split(";")[0]);
        urls.add(ss.split(";")[1]);
        urls.add(ss.split(";")[2]);
        Picasso.with(context).load(urls.get(0)).into(image1);
        Picasso.with(context).load(urls.get(1)).into(image2);
        Picasso.with(context).load(urls.get(2)).into(image3);

        delete = findViewById(R.id.delete);
        delete.setOnClickListener(this);
        String phone = detail.getPhone();
        List<UserBean> userBeans = LitePal.findAll(UserBean.class);
        if (userBeans != null && userBeans.size() == 1) {
            UserBean userBean = userBeans.get(0);
            if (userBean.getPhoneNumber().equals(phone)) {
                delete.setVisibility(View.VISIBLE);
            } else {
                delete.setVisibility(View.GONE);
            }
        } else {
            delete.setVisibility(View.GONE);
        }
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
            case R.id.delete:
                deleteDetail();
                break;
        }
    }

    private void deleteDetail() {
        detail.delete(new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if (e == null) {
                    ToastUtil.normalShow(MeSendActivity.this, "删除成功!", true);
                } else {
                    ToastUtil.normalShow(MeSendActivity.this, "删除失败!", true);
                }
            }

        });
    }

}
