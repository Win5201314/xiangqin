package com.zsl.itgod;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.zsl.Util.ToastUtil;
import com.zsl.bean.JobBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class JobActivity extends BaseActivity {

    private JobBean jobBean;

    private AppCompatButton delete;
    private AppCompatTextView msg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.job);
        delete = findViewById(R.id.delete);
        msg = findViewById(R.id.msg);
        Intent intent = getIntent();
        if (intent == null) finish();
        jobBean = (JobBean) intent.getSerializableExtra("job");
        StringBuilder m = new StringBuilder();
        m.append("公司名字:" + jobBean.getCompanyName() + "\n");
        m.append("薪资范围:" + jobBean.getSalary() + "\n");
        m.append("福利待遇:" + jobBean.getOther() + "\n");
        m.append("具体技术要求:" + jobBean.getDemand() + "\n");
        m.append("办公地址:" + jobBean.getAddress() + "\n");
        m.append("联系方式:" + jobBean.getContact() + "\n");
        m.append("是否外包性质:" + (jobBean.isOutsource() ? "外包性质" : "非外包性质"));
        msg.setText(m.toString());
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fun(jobBean.getObjectId());
            }
        });
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = format.parse(jobBean.getDate());
            int day = differentDaysByMillisecond(new Date(), start);
            if (day >= 30) fun(jobBean.getObjectId());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void fun(String id) {
        jobBean.setObjectId(id);
        jobBean.delete(new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if (e == null) {
                    ToastUtil.normalShow(JobActivity.this, "删除成功!", true);
                } else {
                    ToastUtil.normalShow(JobActivity.this, "删除失败!", true);
                }
            }

        });
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }
}
