package com.socialassistant_youyuelive.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.util.ShowToast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserAgreementActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.user_agreement_next_btn) Button next_btn;
    @BindView(R.id.user_agreement_checkbox) AppCompatCheckBox user_agreement_checkbox;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_agreement);
        initToolbar(R.id.toolbar, R.id.title, "用户协议");
        context = this;
        ButterKnife.bind(this);
        next_btn.setOnClickListener(this);
        requestPermissionAndroid_6();
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

    private void requestPermissionAndroid_6() {
        if (Build.VERSION.SDK_INT < 23) return;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserAgreementActivity.this,
                    new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    ShowToast.show(context, R.string.NO_PERMISSION, true);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.user_agreement_next_btn:{
                if (user_agreement_checkbox.isChecked()) {
                    startActivity(new Intent(this, BecomeAnchorActivity.class));
                    //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
                    int v1 = Integer.valueOf(android.os.Build.VERSION.SDK);
                    if(v1 >= 5) {
                        //此为自定义的动画效果
                        overridePendingTransition(R.anim.en, R.anim.ex);
                    }
                } else {
                    ShowToast.show(context, R.string.agree_protocol_before, true);
                }
                break;
            }
        }
    }

}
