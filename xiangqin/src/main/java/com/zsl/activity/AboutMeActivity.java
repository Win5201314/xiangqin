package com.zsl.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.zsl.Util.ToastUtil;
import com.zsl.Util.UrlUtil;
import com.zsl.Util.UtilTools;
import com.zsl.xiangqin.R;

public class AboutMeActivity extends BaseActivity {

    private AppCompatTextView showPC;
    private AppCompatButton copy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutme);
        String versionName = UtilTools.getVersion(AboutMeActivity.this);
        setTitle("关于本社区(" + versionName + ")");
        showPC = findViewById(R.id.showPC);
        showPC.setText(UrlUtil.PC);
        copy = findViewById(R.id.copy);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UtilTools.copyToClipboard(AboutMeActivity.this, UrlUtil.PC);
                ToastUtil.normalShow(AboutMeActivity.this, "网站网址复制成功!", true);
            }
        });
    }

}
