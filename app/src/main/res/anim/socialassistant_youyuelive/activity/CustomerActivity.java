package com.socialassistant_youyuelive.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.util.ShowToast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomerActivity extends BaseActivity {

    private Context context;
    @BindView(R.id.kf) TextView kf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        context = this;
        ButterKnife.bind(this);
        initToolbar(R.id.toolbar, R.id.title, "客服");
        kf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击复制微信号，方便直接粘贴
                String text = "107428081";
                ClipboardManager c = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                try {
                    c.setPrimaryClip(ClipData.newPlainText("Label", text));
                    ShowToast.normalShow(context, "复制微信号成功!", true);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        Toolbar toolbar = (Toolbar) findViewById(id);
        //toolbar.setTitle("");
        TextView textView = (TextView) findViewById(titleId);
        textView.setText(titleString);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
    }

}
