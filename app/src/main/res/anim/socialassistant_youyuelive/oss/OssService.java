package com.socialassistant_youyuelive.oss;

import android.app.IntentService;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import android.content.Intent;

import android.content.Context;
import android.util.Log;

public class OssService extends IntentService {
    private OSS oss;

    public OSS getOss() {
        return oss;
    }


    public void setOss(OSS oss) {
        this.oss = oss;
    }


    public OssService() {
        super("DownloadIntentService");
    }

//	public class LocalBinder extends Binder {
//		OssService getService() {
//			return OssService.this;
//		}
//	}
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		// TODO Auto-generated method stub
//		return new LocalBinder();
//	}
//

    @Override  //建议上传在xp里实现，下载在APP里实现
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        String bucket = OSSUtil.BUCKETNAME;
        String object = "images/appdemotest-001.jpg";
        String localFile = "sdcard/chongsoft/images/appdemotest-001.jpg";
        //oss上传文件
        OSSUtil.asyncPutFile(oss, null, -1, bucket, object, localFile, -1);
//		Thread td=new Thread()
//		{
//			public void run()
//			{
//				//oss同步下载文件
//				//OSSUtil.downloadOssFileSynchronize(oss,"cctest001","images/appdemotest.jpg","sdcard/chongsoft/saves");
//				//普通下载文件
//				//OSSUtil.downloadFile("http://cctest001.oss-cn-shenzhen.aliyuncs.com/images/appdemotest2.jpg","sdcard/chongsoft/saves/images/appdemotest2.jpg");
//			}
//		};
//		td.start();
        //oss异步下载文件
        //OSSUtil.downloadOssFileAsync(oss,"cctest001","images/appdemotest.jpg","sdcard/chongsoft/saves");


    }

    @Override
    public void onCreate() {
        super.onCreate();
        //OSS初始化
        oss = initOSS(this, OSSUtil.REGON_HOST, OSSUtil.STSGET_SERVER, OSSUtil.BUCKETNAME);
    }

    //初始化一个OssService用来上传下载
    public static OSSClient initOSS(Context context, String endpoint, String stsServer, String bucket) {
        Log.d("A", endpoint);
        //如果希望直接使用accessKey来访问的时候，可以直接使用OSSPlainTextAKSKCredentialProvider来鉴权。
        //OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);
        //使用自己的获取STSToken的类
        OSSCredentialProvider credentialProvider = new STSGetter(stsServer);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSSClient oss = new OSSClient(context, endpoint, credentialProvider, conf);
        return oss;
    }


}

