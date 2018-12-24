package com.zsl.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.squareup.picasso.Picasso;
import com.zsl.Util.ToastUtil;
import com.zsl.Util.UtilTools;
import com.zsl.bean.Detail;
import com.zsl.bean.LikeBean;
import com.zsl.xiangqin.R;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class DetailActivity extends BaseActivity implements View.OnClickListener {

    private AppCompatTextView message;
    private AppCompatImageView image1, image2, image3;
    private Button delete;
    private Button likeOther;
    private Context context;
    private Detail detail = new Detail();

    private ArrayList<String> urls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
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
        if (UtilTools.isBoss()) delete.setVisibility(View.VISIBLE);
        likeOther = findViewById(R.id.likeOther);
        likeOther.setOnClickListener(this);
        if (UtilTools.loginedPhone().equals(detail.getPhone())) {
            likeOther.setVisibility(View.GONE);
        }

        String like = intent.getStringExtra("like");
        if (!TextUtils.isEmpty(like)) {
            delete.setVisibility(View.GONE);
            likeOther.setVisibility(View.GONE);
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
            case R.id.likeOther: {
                ToastUtil.normalShow(context, "在发送,莫慌!", true);
                likeOtherDetail();
                break;
            }
        }
    }

    private void likeOtherDetail() {
        //先将此人信息查出来
        BmobQuery<LikeBean> query = new BmobQuery<>();
        //查询playerName叫“比目”的数据
        query.addWhereEqualTo("phone", detail.getPhone());
        //返回1条数据，如果不加上这条语句，默认返回10条数据
        query.setLimit(1);
        //最新数据优先排出
        query.order("-createdAt");
        //执行查询方法
        query.findObjects(new FindListener<LikeBean>() {
            @Override
            public void done(List<LikeBean> object, BmobException e) {
                if (e == null) {
                    Log.d("TAG", "=======================1");
                    if (object.size() <= 0) {
                        Log.d("TAG", "=======================2");
                        LikeBean likeBean = new LikeBean();
                        likeBean.setPhone(detail.getPhone());
                        likeBean.setLikeMe(UtilTools.loginedPhone());
                        likeBean.setLikeOther("");
                        likeBean.save(new SaveListener<String>() {
                            @Override
                            public void done(String objectId,BmobException e) {
                                Log.d("TAG", "=======================3");
                                if (e == null) {
                                    updateLikeOther();
                                    ToastUtil.normalShow(context, "成功!", true);
                                } else {
                                    ToastUtil.normalShow(context, "喜欢失败!", true);
                                }
                            }
                        });
                        //ToastUtil.normalShow(context, "不存在,可能已被移除!", true);
                        return;
                    }
                    Log.d("TAG", "=======================4");
                    LikeBean likeBean = object.get(0);
                    String likeMe = likeBean.getLikeMe();
                    String[] lm = likeMe.split(",");
                    String lo = UtilTools.loginedPhone();
                    boolean flag = false;
                    String d = "";
                    for (String s : lm) { flag = s.equals(lo); if (flag) break; d = (d + s + ","); }
                    if (flag) {
                        ToastUtil.normalShow(context, "之前点击喜欢过!", true);
                    } else {
                        d = d + lo;
                        likeBean.setLikeMe(d);
                        likeBean.update(likeBean.getObjectId(), new UpdateListener() {

                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    //toast("更新成功:"+p2.getUpdatedAt());
                                    ToastUtil.normalShow(context, "喜欢成功!", true);
                                    updateLikeOther();
                                } else {
                                    //toast("更新失败：" + e.getMessage());
                                }
                            }

                        });
                    }
                } else {
                    Log.d("TAG", "=======================9" + e.getMessage());
                    LikeBean likeBean = new LikeBean();
                    likeBean.setPhone(detail.getPhone());
                    likeBean.setLikeMe(UtilTools.loginedPhone());
                    likeBean.setLikeOther("");
                    likeBean.save(new SaveListener<String>() {
                        @Override
                        public void done(String objectId,BmobException e) {
                            Log.d("TAG", "===8====================3");
                            if (e == null) {
                                updateLikeOther();
                                ToastUtil.normalShow(context, "成功!", true);
                            } else {
                                ToastUtil.normalShow(context, "喜欢失败!", true);
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateLikeOther() {
        //先将本人信息查出来
        BmobQuery<LikeBean> query = new BmobQuery<>();
        //查询playerName叫“比目”的数据
        query.addWhereEqualTo("phone", UtilTools.loginedPhone());
        //返回1条数据，如果不加上这条语句，默认返回10条数据
        query.setLimit(1);
        //最新数据优先排出
        query.order("-createdAt");
        //执行查询方法
        query.findObjects(new FindListener<LikeBean>() {
            @Override
            public void done(List<LikeBean> object, BmobException e) {
                if (e == null) {
                    Log.d("TAG", "==================6");
                    if (object.size() <= 0) {
                        Log.d("TAG", "==================7");
                        LikeBean likeBean = new LikeBean();
                        likeBean.setPhone(UtilTools.loginedPhone());
                        likeBean.setLikeMe("");
                        likeBean.setLikeOther(detail.getPhone());
                        likeBean.save(new SaveListener<String>() {
                            @Override
                            public void done(String objectId,BmobException e) {
                                if (e == null) {
                                    ToastUtil.normalShow(context, "成功!", true);
                                } else {
                                    ToastUtil.normalShow(context, "喜欢失败!", true);
                                }
                            }
                        });
                        //ToastUtil.normalShow(context, "不存在,可能已被移除!", true);
                        return;
                    }
                    LikeBean likeBean = object.get(0);
                    String likeOther = likeBean.getLikeOther();
                    String[] lm = likeOther.split(",");
                    String lo = detail.getPhone();
                    boolean flag = false;
                    String d = "";
                    for (String s : lm) { flag = s.equals(lo); if (flag) break; d = (d + s + ","); }
                    if (flag) {
                        ToastUtil.normalShow(context, "互相喜欢，快去加微信吧!", true);
                    } else {
                        d = d + lo;
                        likeBean.setLikeOther(d);
                        likeBean.update(likeBean.getObjectId(), new UpdateListener() {

                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    //toast("更新成功:"+p2.getUpdatedAt());
                                } else {
                                    //toast("更新失败：" + e.getMessage());
                                }
                            }

                        });
                    }
                } else {
                    Log.d("TAG", "==================6" + e.getMessage());
                }
            }
        });
    }

    private void deleteDetail() {
        detail.delete(new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if (e == null) {
                    ToastUtil.normalShow(DetailActivity.this, "删除成功!", true);
                } else {
                    ToastUtil.normalShow(DetailActivity.this, "删除失败!", true);
                }
            }

        });
    }
}
