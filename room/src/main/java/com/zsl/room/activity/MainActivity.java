package com.zsl.room.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.zsl.room.R;
import com.zsl.room.adapter.ViewPagerAdapter;
import com.zsl.room.fragment.HeZuFragment;
import com.zsl.room.fragment.QZFragment;
import com.zsl.room.fragment.ZZFragment;
import com.zsl.room.util.ActivityCollector;
import com.zsl.room.util.ToastUtil;

import cn.bmob.v3.Bmob;


public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView bottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:viewPager.setCurrentItem(0);
                    break;
                case R.id.navigation_dashboard:viewPager.setCurrentItem(1);
                    break;
                case R.id.navigation_notifications:viewPager.setCurrentItem(2);
                    break;
            }
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCollector.addActivity(this);

        bottomNavigationView = findViewById(R.id.navigation);
        viewPager = findViewById(R.id.viewpager);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        /*//禁止ViewPager滑动
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });*/

        setupViewPager(viewPager);

        //创建数据库表
        //LitePal.getDatabase();

        Bmob.initialize(this, "cc43a68620b6c9fcafb97365939a7e22");

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

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new HeZuFragment());
        adapter.addFragment(new HeZuFragment());
        adapter.addFragment(new HeZuFragment());
        viewPager.setAdapter(adapter);
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
            case R.id.action_group: {
                startActivity(new Intent(this, GroupActivity.class));
                break;
            }
            case R.id.action_send: {
                startActivity(new Intent(this, SendActivity.class));
                break;
            }
            case R.id.action_qz: {
                startActivity(new Intent(this, QZActivity.class));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
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

}
