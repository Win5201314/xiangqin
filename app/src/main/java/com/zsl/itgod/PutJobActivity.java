package com.zsl.itgod;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.zsl.Util.Logger;
import com.zsl.Util.ToastUtil;
import com.zsl.bean.JobBean;

import java.util.Calendar;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class PutJobActivity extends BaseActivity {

    private AppCompatEditText companyName, salary, other, demand, address, contact;
    private AppCompatButton put;
    private RadioGroup type, outsource;
    private RadioButton android, ios, java, web, ui, cpp, c, nx;
    private RadioButton yes, no;
    private JobBean jobBean;
    private int T = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.putjob);
        initView();
        jobBean = new JobBean();
        jobBean.setType(T);
        jobBean.setOutsource(false);
        type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.android:T = 0;break;
                    case R.id.IOS: T = 1; break;
                    case R.id.Java: T = 2; break;
                    case R.id.web: T = 3; break;
                    case R.id.UI: T = 4; break;
                    case R.id.CPP: T = 5; break;
                    case R.id.C: T = 6; break;
                    case R.id.nx: T = 7; break;
                }
                jobBean.setType(T);
                Logger.d("TAG", jobBean.getType() + "<<<<");
            }
        });
        outsource.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                jobBean.setOutsource(i == 0);
            }
        });
        put.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fun();
            }
        });
    }

    private void fun() {
        if (jobBean.getType() == -1) return;
        String n = companyName.getText().toString();
        if (TextUtils.isEmpty(n)) return;
        String s = salary.getText().toString();
        if (TextUtils.isEmpty(s)) return;
        String o = other.getText().toString();
        if (TextUtils.isEmpty(o)) return;
        String d = demand.getText().toString();
        if (TextUtils.isEmpty(d)) return;
        String a = address.getText().toString();
        if (TextUtils.isEmpty(a)) return;
        String c = contact.getText().toString();
        if (TextUtils.isEmpty(c)) return;
        jobBean.setCompanyName(n);
        jobBean.setSalary(s);
        jobBean.setOther(o);
        jobBean.setDemand(d);
        jobBean.setAddress(a);
        jobBean.setContact(c);
        int y, m, day;
        Calendar cal = Calendar.getInstance();
        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DATE);
        String dd = y + "-" + m + "-" + day;
        Logger.d("TAG", dd);
        jobBean.setDate(dd);
        //上传到比目科技后台
        jobBean.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e == null){
                    ToastUtil.normalShow(PutJobActivity.this, "上传成功!审核成功之后显示出来!", true);
                    finish();
                }else{
                    ToastUtil.normalShow(PutJobActivity.this, "上传失败!", true);
                }
            }
        });
    }

    private void initView() {
        companyName = findViewById(R.id.companyName);
        salary = findViewById(R.id.salary);
        other = findViewById(R.id.other);
        demand = findViewById(R.id.demand);
        address = findViewById(R.id.address);
        contact = findViewById(R.id.contact);
        put = findViewById(R.id.put);
        type = findViewById(R.id.type);
        outsource = findViewById(R.id.outSource);
        android = findViewById(R.id.android);
        ios = findViewById(R.id.IOS);
        java = findViewById(R.id.Java);
        web = findViewById(R.id.web);
        ui = findViewById(R.id.UI);
        cpp = findViewById(R.id.CPP);
        c = findViewById(R.id.C);
        nx = findViewById(R.id.nx);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
    }
}
