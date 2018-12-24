package com.zsl.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.zsl.Util.ActivityCollector;
import com.zsl.Util.ToastUtil;
import com.zsl.Util.UrlUtil;
import com.zsl.Util.UtilTools;
import com.zsl.adapter.UserImageAdapter;
import com.zsl.bean.Detail;
import com.zsl.bean.UserBean;
import com.zsl.bean.UserImage;
import com.zsl.xiangqin.LoginActivity;
import com.zsl.xiangqin.R;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class MainActivity extends AppCompatActivity implements UserImageAdapter.OnItemClickListener {

    private SwipeRefreshLayout mSwipeRefreshWidget;
    private RecyclerView recyclerView;
    private List<UserImage> userImages = new ArrayList<>();
    private List<Detail> details = new ArrayList<>();
    private UserImageAdapter userImageAdapter;
    private static int lastVisibleItem = 0;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:{
                    mSwipeRefreshWidget.setRefreshing(false);
                    userImageAdapter.notifyDataSetChanged();
                    break;
                }
                case 1: {
                    mSwipeRefreshWidget.setRefreshing(false);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCollector.addActivity(this);

        if (UtilTools.isBoss()) {
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, CheckActivity.class));
                }
            });
        }

        mSwipeRefreshWidget = findViewById(R.id.swipe_refresh_widget);

        mSwipeRefreshWidget.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        mSwipeRefreshWidget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onrefresh();
            }

        });

        // 这句话是为了，第一次进入页面的时候显示加载进度条
        mSwipeRefreshWidget.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));

        recyclerView = findViewById(R.id.rv);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper. VERTICAL);
        //设置Adapter
        userImageAdapter = new UserImageAdapter(userImages, this);
        recyclerView.setAdapter(userImageAdapter);
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator( new DefaultItemAnimator());
        //设置列表点击事件
        userImageAdapter.setOnItemClickListener(this);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == userImageAdapter.getItemCount()) {
                    onrefresh();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }

        });

        //创建数据库表
        LitePal.getDatabase();

        Bmob.initialize(this, "6156cdcfe52926bc04a90d231ad6a1b1");

        //申请外部存储权限6.0
        AndPermission.with( this) .requestCode(100) .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE) .send();
        //申请拒绝后再次申请
        AndPermission.with( this) .requestCode(100) .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE) .rationale(new RationaleListener() {
            @Override
            public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                // 此对话框可以自定义,调用rationale.resume()就可以继续申请。
                AndPermission.rationaleDialog(MainActivity.this, rationale).show();
            }
        }) .send();

        onrefresh();
    }

    //刷新获取后台数据
    private void onrefresh() {
        mSwipeRefreshWidget.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                BmobQuery<Detail> query = new BmobQuery<>();
                //查询playerName叫“比目”的数据
                //query.addWhereEqualTo("type", type);
                //返回50条数据，如果不加上这条语句，默认返回10条数据
                query.setLimit(50);
                //最新数据优先排出
                query.order("-createdAt");
                //执行查询方法
                query.findObjects(new FindListener<Detail>() {
                    @Override
                    public void done(List<Detail> object, BmobException e) {
                        if (e == null) {
                            if (object.size() <= 0) {
                                handler.sendEmptyMessage(1);
                                return;
                            }
                            if (details.size() < 50) {
                                List<Detail> details2 = new ArrayList<>();
                                for (Detail detail : object) {
                                    if (isNeedAdd(detail)) details2.add(detail);
                                }
                                details.addAll(details2);
                                for (Detail detail : details2) {
                                    userImages.add(new UserImage(detail.getImageUrl().split(";")[0], detail.getPath(), detail.getResidence(), detail.getBirthday()));
                                }
                            } else {
                                details.addAll(object);
                                for (Detail detail : object) {
                                    userImages.add(new UserImage(detail.getImageUrl().split(";")[0], detail.getPath(), detail.getResidence(), detail.getBirthday()));
                                }
                            }
                            handler.sendEmptyMessage(0);
                        } else {
                            handler.sendEmptyMessage(1);
                        }
                    }
                });
            }
        }).start();
    }

    public boolean isNeedAdd(Detail d) {
        String phoneNumber = d.getPhone();
        boolean flag = false;
        for (Detail detail : details) {
            flag = phoneNumber.equals(detail.getPhone());
            if (flag) return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search: {
                if (UtilTools.isLogined()) {
                    showSearchDialog();
                } else {
                    //进入登录界面
                    startActivity(new Intent(this, LoginActivity.class));
                }
                break;
            }
            case R.id.action_likeMe: {
                if (UtilTools.isLogined()) {
                    startActivity(new Intent(this, LikeActivity.class));
                } else {
                    ToastUtil.show(this, R.string.needLogin, true);
                    startActivity(new Intent(this, LoginActivity.class));
                }
                break;
            }
            case R.id.action_likeOther: {
                if (UtilTools.isLogined()) {
                    startActivity(new Intent(this, LikeOtherActivity.class));
                } else {
                    ToastUtil.show(this, R.string.needLogin, true);
                    startActivity(new Intent(this, LoginActivity.class));
                }
                break;
            }
            case R.id.action_joinGroup: {
                startActivity(new Intent(this, JoinGroupActivity.class));
                break;
            }
            case R.id.action_send: {
                startActivity(new Intent(this, AgreeActivity.class));
                break;
            }
            case R.id.action_mesend: {
                List<UserBean> userBeans = LitePal.findAll(UserBean.class);
                if (userBeans != null && userBeans.size() == 1) {
                    meSend(userBeans.get(0).getPhoneNumber());
                } else {
                    //进入登录界面
                    startActivity(new Intent(this, LoginActivity.class));
                }
                break;
            }
            case R.id.action_news: {
                startActivity(new Intent(this, NewsActivity.class));
                break;
            }
            case R.id.action_share: {
                showShare();
                break;
            }
            case R.id.action_me: {
                startActivity(new Intent(this, AboutMeActivity.class));
                break;
            }
            case R.id.action_exit: {
                if (UtilTools.isLogined()) {
                    showExitDialog();
                } else {
                    ToastUtil.normalShow(this, "当前没有登录账号!", true);
                    //进入登录界面
                    startActivity(new Intent(this, LoginActivity.class));
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void meSend(final String phone) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BmobQuery<Detail> query = new BmobQuery<>();
                //查询playerName叫“比目”的数据
                query.addWhereEqualTo("phone", phone);
                //返回1条数据，如果不加上这条语句，默认返回10条数据
                query.setLimit(1);
                //最新数据优先排出
                query.order("-createdAt");
                //执行查询方法
                query.findObjects(new FindListener<Detail>() {
                    @Override
                    public void done(List<Detail> object, BmobException e) {
                        if (e == null) {
                            if (object.size() <= 0) {
                                ToastUtil.normalShow(MainActivity.this, "不存在!", true);
                                return;
                            }
                            Detail detail = object.get(0);
                            if (detail == null) return;
                            Intent intent = new Intent(MainActivity.this, MeSendActivity.class);
                            intent.putExtra("detail", detail);
                            startActivity(intent);
                        }
                    }
                });
            }
        }).start();
    }

    private void showExitDialog() {
        final Context context = MainActivity.this;
        new AlertDialog.Builder(context)
                .setTitle("退出当前账号")
                .setCancelable(true)
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //清空数据库
                        LitePal.deleteAll(UserBean.class);
                        //进入登录界面
                        startActivity(new Intent(context, LoginActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    public void showSearchDialog() {
        final Context context = MainActivity.this;
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_search, null);
        final AppCompatEditText phone = view.findViewById(R.id.phone);
        new AlertDialog.Builder(context)
                .setTitle("查找[手机号查找]")
                .setCancelable(true)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        meSend(phone.getText().toString());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ToastUtil.show(this, R.string.exit_app, true);
                exitTime = System.currentTimeMillis();
            } else {
                ActivityCollector.finishAll();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(int position) {
        //查找数据库
        if (UtilTools.isLogined()) {
            if (details.size() > position) {
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra("detail", details.get(position));
                startActivity(intent);
            }
        } else {
            ToastUtil.show(this, R.string.needLogin, true);
            //没登录，就去登录
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void showShare() {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // title标题，微信、QQ和QQ空间等平台使用
        oks.setTitle(getString(R.string.app_name));
        // titleUrl QQ和QQ空间跳转链接
        oks.setTitleUrl(UrlUtil.PC_MainPage);
        // text是分享文本，所有平台都需要这个字段
        oks.setText("自由相亲社区,有你的另一半!");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.dsg);
        oks.setImageData(bitmap);
        // url在微信、微博，Facebook等平台中使用
        oks.setUrl(UrlUtil.PC_MainPage);
        // comment是我对这条分享的评论，仅在人人网使用
        //oks.setComment("自由相亲社区,有你的另一半!");

        // 启动分享GUI
        oks.show(this);
    }

    private static final int REQUEST_CODE_SETTING = 100;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 只需要调用这一句,其它的交给AndPermission吧,最后一个参数是PermissionListener。
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, listener);
    }

    private PermissionListener listener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, List<String> grantedPermissions) {
            // 权限申请成功回调。
            if (requestCode == 100) {
                // TODO 相应代码。
                //ToastUtil.normalShow(MainActivity.this, "申请权限成功!", true);
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            // 权限申请失败回调。
            // 用户否勾选了不再提示并且拒绝了权限,那么提示用户到设置中授权。
            if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, deniedPermissions)) {
                // 第一种:用默认的提示语。
                //AndPermission.defaultSettingDialog(MainActivity.this, REQUEST_CODE_SETTING).show();
                // 第二种:用自定义的提示语。
                AndPermission.defaultSettingDialog(MainActivity.this, REQUEST_CODE_SETTING).setTitle("权限申请失败").setMessage("我们需要的一些权限被您拒绝或者系统发生错误申请失败,请您到设置页面手动授权,否则功能无法正常使用!").setPositiveButton("好,去设置").show();
                /*// 第三种:自定义dialog样式。
                SettingService settingService = AndPermission.defineSettingDialog(MainActivity.this, REQUEST_CODE_SETTING);
                // 你的dialog点击了确定调用:
                settingService.execute();
                // 你的dialog点击了取消调用:
                settingService.cancel();*/
            }
        }
    };

}
