package com.socialassistant_youyuelive.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.socialassistant_youyuelive.R;
import com.tapadoo.alerter.Alerter;

/**
 * Created by Administrator on 2017/6/1.
 */

public class TagActivity extends BaseActivity implements View.OnClickListener {

    private TextView keep;
    private EditText tag_my;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_my);
        initToolbar(R.id.toolbar, R.id.title, "自定义标签");
        tag_my = (EditText) findViewById(R.id.tag_my);
        tag_my.setHint("每个标签最多4个字!");
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        Toolbar toolbar = (Toolbar) findViewById(id);
        //toolbar.setTitle("");
        TextView textView = (TextView) findViewById(titleId);
        textView.setText(titleString);
        keep = (TextView) findViewById(R.id.keep);
        keep.setOnClickListener(this);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.keep:
                if (tag_my.getText() != null) {
                    String tag = tag_my.getText().toString();
                    if (!TextUtils.isEmpty(tag) && tag.length() > 0 && tag.length() <= 4) {
                        Intent intent = new Intent(TagActivity.this, ChangeTagActivity.class);
                        intent.putExtra("my_tag", tag);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Alerter.create(TagActivity.this)
                                .setTitle("自定义标签")
                                .setText("每个标签最多4个字...")
                                .setBackgroundColorRes(R.color.colorAccent)
                                // or setBackgroundColorInt(Color.CYAN)
                                .enableSwipeToDismiss()
                                .setDuration(3000)
                                .show();
                    }
                }
                break;
        }
    }

}
