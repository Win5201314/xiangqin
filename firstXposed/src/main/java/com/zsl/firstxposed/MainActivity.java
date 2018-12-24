package com.zsl.firstxposed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btn;
    private static int n = 0;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:{
                    btn.setText(n + "");
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //toastMessage();
                deleteContacts();
            }
        });

        //申请外部存储权限6.0
        AndPermission.with( this) .requestCode(100) .permission(Manifest.permission.READ_CONTACTS) .send();
        //申请拒绝后再次申请
        AndPermission.with( this) .requestCode(100) .permission(Manifest.permission.READ_CONTACTS) .rationale(new RationaleListener() {
            @Override
            public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                // 此对话框可以自定义,调用rationale.resume()就可以继续申请。
                AndPermission.rationaleDialog(MainActivity.this, rationale).show();
            }
        }) .send();
        EventBus.getDefault().register(this);
        findViewById(R.id.bn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SecActivity.class);
                startActivity(intent);
            }
        });
    }

    private void deleteContacts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                int deletcount = 0;
                while (cursor.moveToNext()) {
                    String lookup = cursor.getString(cursor.getColumnIndex("lookup"));
                    Uri lookupuri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookup);
                    String id = (cursor).getString(cursor.getColumnIndex("_id"));

                    // 明天试一下用ContentResolver批处理，而不是每条删除
                    cr.delete(lookupuri, "_id =? ", new String[]{"" + id});
                    deletcount++;
                    n = deletcount;
                    handler.sendEmptyMessage(0);
                }
            }
        }).start();
        //log("========" + deletcount);
    }

    public static void log(String content) {
        wrtieFile("/sdcard/chongsoft/logL.txt", content + "\r\n", true, "utf-8");
    }

    public static void wrtieFile(String filenPath, String content, boolean append, String encode) {
        File file = new File(filenPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(file, append);
            fos.write(content.getBytes(encode));
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String toastMessage() {
        return "我未被劫持";
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent) {
        btn.setText(messageEvent.getMessage());
    }

}
