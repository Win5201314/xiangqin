package com.socialassistant_youyuelive.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.alibaba.sdk.android.oss.OSSClient;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.oss.OSSUtil;
import com.socialassistant_youyuelive.oss.OssService;

import java.io.File;

/**
 * Created by Administrator on 2017/11/11.
 */

public class UploadService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pathImages[0] = intent.getStringExtra("face");
        pathImages[1] = intent.getStringExtra("album_1");
        pathImages[2] = intent.getStringExtra("album_2");
        pathImages[3] = intent.getStringExtra("album_3");
        //pathImages[4] = intent.getStringExtra("album_4");
        //pathImages[5] = intent.getStringExtra("album_5");
        pathVedio1 = intent.getStringExtra("vedio");
        //pathAudio = intent.getStringExtra("voice");
        upLoadUser();
        return super.onStartCommand(intent, flags, startId);
    }

    //上传bitmaps对应的6张图片，一段视频，一段语音
    private static String[] pathImages = new String[4];
    private static String pathVedio1, pathAudio;
    private void upLoadUser() {
        //文件oss直传上服务器
        String bucket = OSSUtil.BUCKETNAME;
        //视频
        String name = pathVedio1.substring(pathVedio1.lastIndexOf("/") + 1, pathVedio1.length());
        String objectVedio = bucket + File.separator + ConstString.mobile + File.separator + name;
        //语音
        //name = pathAudio.substring(pathAudio.lastIndexOf("/") + 1, pathAudio.length());
        //String objectAudio = bucket + File.separator + ConstString.mobile + File.separator + name;
        OSSClient oss = OssService.initOSS(this, OSSUtil.REGON_HOST, ConstString.SIGN, OSSUtil.BUCKETNAME);
        //上传图片文件
        String name_format;
        for (int i = 0; i < pathImages.length; i++) {
            File fileImage = new File(pathImages[i]);
            if (fileImage != null && fileImage.exists()) {
                //图片(.png)
                name_format = pathImages[i].substring(pathImages[i].lastIndexOf("."), pathImages[i].length());
                String objectImage = bucket + File.separator + ConstString.mobile + File.separator + System.currentTimeMillis() + i + name_format;
                if (i == 0) {
                    OSSUtil.asyncPutFile(oss, null, 3, bucket, objectImage, pathImages[i], -1);
                } else {
                    OSSUtil.asyncPutFile(oss, null, 0, bucket, objectImage, pathImages[i], -1);
                }
            }
        }
        //上传视频文件
        File Vedio = new File(pathVedio1);
        if (Vedio != null && Vedio.exists()) OSSUtil.asyncPutFile(oss, null, 1, bucket, objectVedio, pathVedio1, -1);

        //上传语音文件
        //File Audio = new File(pathAudio);
        //if (Audio != null && Audio.exists()) OSSUtil.asyncPutFile(oss, null, 2, bucket, objectAudio, pathAudio, -1);
    }

}
