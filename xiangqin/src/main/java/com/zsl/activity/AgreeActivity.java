package com.zsl.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.Button;

import com.zsl.Util.ToastUtil;
import com.zsl.bean.UserBean;
import com.zsl.xiangqin.LoginActivity;
import com.zsl.xiangqin.R;

import org.litepal.LitePal;

import java.util.List;

public class AgreeActivity extends BaseActivity {

    private AppCompatCheckBox box;
    private Button agree;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agree);
        agree = findViewById(R.id.agree);
        box = findViewById(R.id.box);
        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean flag = box.isChecked();
                if (flag) {
                    /*List<UserBean> userBeans = LitePal.findAll(UserBean.class);
                    if (userBeans != null && userBeans.size() == 1) {
                        startActivity(new Intent(AgreeActivity.this, SubmitActivity.class));
                    } else {
                        ToastUtil.show(this, R.string.needLogin, true);
                        //没登录，就去登录
                        startActivity(new Intent(AgreeActivity.this, LoginActivity.class));
                        finish();
                    }*/
                    startActivity(new Intent(AgreeActivity.this, SubmitActivity.class));
                } else {
                    ToastUtil.normalShow(AgreeActivity.this, "同意并发布!", true);
                }
            }
        });
    }
}
