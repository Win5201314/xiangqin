package com.socialassistant_youyuelive.oss;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.socialassistant_youyuelive.AV.DemoApplication;
import com.socialassistant_youyuelive.activity.AlbumActivity;
import com.socialassistant_youyuelive.activity.HeadActivity;
import com.socialassistant_youyuelive.activity.MeActivity;
import com.socialassistant_youyuelive.activity.ReportActivity;
import com.socialassistant_youyuelive.commomentity.ConstString;

public class OSSUtil {
    public static String STSGET_SERVER = ConstString.OSS_IP + "/video/appTokenServer/getStsToken";//ConstString.URL_ROOT + "getStsToken";
    public static String CALLBACK_SERVER = ConstString.OSS_IP + "/video/appTokenServer/ossCallbackAppupload";//ConstString.URL_ROOT + "ossCallbackAppupload";
    public static String REGON_HOST = "https://oss-cn-shenzhen.aliyuncs.com";
    public static String BUCKETNAME = "youyue-oss";
    // public static String ENDPOINT_HOST =
    // "https://chongsoft-oss.oss-cn-shenzhen.aliyuncs.com";

    public OSSUtil() {
        // TODO Auto-generated constructor stub
    }

    public static void asyncPutImageFile(OSS oss, final String bucket, final String object, File localFile,
                                         final String talker, final String source, final String uid, final String username,
                                         final String friendNickname, final String ccmessageNickname, final String fromUser, final int bigimg,
                                         final String ossType, final String contentType) {
        if (object.equals("")) {
            Log.d("Api", "ObjectNull");
            return;
        }

        // 构造上传请求
        final PutObjectRequest put = new PutObjectRequest(bucket, object, localFile.getAbsolutePath());

        if (CALLBACK_SERVER != null && bigimg == 0) {
            // 传入对应的上传回调参数，这里默认使用OSS提供的公共测试回调服务器地址
            put.setCallbackParam(new HashMap<String, String>() {
                {
                    put("callbackUrl", CALLBACK_SERVER);
                    // put("callbackHost", hostaddress);
                    // callbackBody可以自定义传入的信息

                    put("callbackBody", "filename=${object}&mimeType=${mimeType}" + "&ossType=${x:ossType}" +
                            // "&ccmessgaeContentType=${x:ccmessgaeContentType}"
                            // +
                            "&ccmessageUid=${x:ccmessageUid}" + "&"
                            + (fromUser == null ? "ccmessgaeContentType" : "ccmessgaeresponeContentType")
                            + "=${x:ccmessgaeContentType}" + "&ccmessageNmae=${x:ccmessageNmae}" +
                            // "&ccmessgaeContent=${object}"+
                            "&" + (fromUser == null ? "ccmessgaeContent" : "ccmessgaeresponeContent") + "=${object}"
                            + "&channelId=${x:channelId}" + "&source=${x:source}"
                            + "&ccmessgaeUsername=${x:ccmessgaeUsername}" + "&ccmessageNickname=${x:ccmessageNickname}"

                    );
                    // "callbackBodyType":"application/json"
                    // "application/x-www-form-urlencoded"
                    put("callbackBodyType", "application/json");

                }
            });

            put.setCallbackVars(new HashMap<String, String>() {
                {
                    put("x:ossType", ossType);// image
                    put("x:ccmessgaeContentType", contentType);
                    put("x:ccmessageUid", uid);
                    // put("x:ccmessageAppType","weixin");
                    put("x:ccmessageNmae", username);

                    try {
                        put("x:ccmessgaeUsername", URLEncoder.encode(friendNickname, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    put("x:channelId", talker);
                    put("x:source", source);
                    put("x:ccmessageNickname", ccmessageNickname);// ccmessageNickname

                }
            });
        }

        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                // Log.d("PutObject", "currentSize: " + currentSize +
                // " totalSize: " + totalSize);
                // int progress = (int) (100 * currentSize / totalSize);
                // UIDisplayer.updateProgress(progress);
                // UIDisplayer.displayInfo("上传进度: " + String.valueOf(progress) +
                // "%");
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                // Log.d("PutObject", "UploadSuccess");

                // Log.d("ETag", result.getETag());
                // Log.d("Api", result.getRequestId());

                // UIDisplayer.uploadComplete();
                // UIDisplayer.displayInfo("Bucket: " + bucket
                // + "\nObject: " + request.getObjectKey()
                // + "\nETag: " + result.getETag()
                // + "\nRequestId: " + result.getRequestId()
                // + "\nCallback: " +
                // result.getServerCallbackReturnBody());
                Log.i("Api",
                        "Bucket: " + bucket + "\nObject: " + request.getObjectKey() + "\nETag: " + result.getETag()
                                + "\nRequestId: " + result.getRequestId() + "\nCallback: "
                                + result.getServerCallbackReturnBody());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion,
                                  ServiceException serviceException) {
                String info = "";
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                    info = clientExcepion.toString();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.d("Api", serviceException.getErrorCode());
                    Log.d("Api", serviceException.getRequestId());
                    Log.d("Api", serviceException.getHostId());
                    Log.d("Api", serviceException.getRawMessage());
                    info = serviceException.toString();
                }
                // UIDisplayer.uploadFail(info);
                // UIDisplayer.displayInfo(info);
                Log.d("Api", info);
            }
        });
    }

    // weixin_chat_audio
    public static void asyncPutVoiceFile(OSS oss, final String bucket, final String object, File localFile,
                                         final String talker, final String source, final String uid, final String username,
                                         final String friendNickname, final String ccmessageNickname, final String extractMetadata,
                                         final String fromUser) {
        if (object.equals("")) {
            Log.w("AsyncPutImage", "ObjectNull");
            return;
        }

        // 构造上传请求
        final PutObjectRequest put = new PutObjectRequest(bucket, object, localFile.getAbsolutePath());

        if (CALLBACK_SERVER != null) {
            // 传入对应的上传回调参数，这里默认使用OSS提供的公共测试回调服务器地址
            put.setCallbackParam(new HashMap<String, String>() {
                {
                    put("callbackUrl", CALLBACK_SERVER);
                    // put("callbackHost", hostaddress);
                    // callbackBody可以自定义传入的信息

                    put("callbackBody", "filename=${object}&mimeType=${mimeType}" + "&ossType=${x:ossType}" +
                            // "&ccmessgaeContentType=${x:ccmessgaeContentType}"
                            // +
                            "&ccmessageUid=${x:ccmessageUid}" + "&"
                            + (fromUser == null ? "ccmessgaeContentType" : "ccmessgaeresponeContentType") + "=audio" +

                            "&ccmessageNmae=${x:ccmessageNmae}" + "&"
                            + (fromUser == null ? "ccmessgaeContent" : "ccmessgaeresponeContent") + "=" + "${object}"
                            + "&channelId=${x:channelId}" + "&source=${x:source}"
                            + "&ccmessgaeUsername=${x:ccmessgaeUsername}" + "&ccmessageNickname=${x:ccmessageNickname}"
                            + "&audioTime=${x:audioTime}");
                    // "callbackBodyType":"application/json"
                    // "application/x-www-form-urlencoded"
                    put("callbackBodyType", "application/json");

                }
            });

            put.setCallbackVars(new HashMap<String, String>() {
                {
                    put("x:ossType", "weixin_chat_audio");// image
                    // put("x:ccmessgaeContentType","audio");
                    put("x:ccmessageUid", uid);

                    put("x:ccmessageNmae", username);
                    try {
                        put("x:ccmessgaeUsername", URLEncoder.encode(friendNickname, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    put("x:channelId", talker);
                    put("x:source", source);
                    put("x:ccmessageNickname", ccmessageNickname);
                    put("x:audioTime", extractMetadata);

                }
            });
        }

        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                // Log.d("PutObject", "currentSize: " + currentSize +
                // " totalSize: " + totalSize);
                // int progress = (int) (100 * currentSize / totalSize);
                // UIDisplayer.updateProgress(progress);
                // UIDisplayer.displayInfo("上传进度: " + String.valueOf(progress) +
                // "%");
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");

                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());

                // UIDisplayer.uploadComplete();
                // UIDisplayer.displayInfo("Bucket: " + bucket
                // + "\nObject: " + request.getObjectKey()
                // + "\nETag: " + result.getETag()
                // + "\nRequestId: " + result.getRequestId()
                // + "\nCallback: " +
                // result.getServerCallbackReturnBody());
                Log.i("upload success",
                        "Bucket: " + bucket + "\nObject: " + request.getObjectKey() + "\nETag: " + result.getETag()
                                + "\nRequestId: " + result.getRequestId() + "\nCallback: "
                                + result.getServerCallbackReturnBody());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion,
                                  ServiceException serviceException) {
                String info = "";
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                    info = clientExcepion.toString();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                    info = serviceException.toString();
                }
                // UIDisplayer.uploadFail(info);
                // UIDisplayer.displayInfo(info);
                Log.d("upload failure", info);
            }
        });
    }

    public static void asyncPutTXTFile(OSS oss, final String bucket, final String object, String localFile, final String ossType) {
        if (object.equals("")) {
            Log.w("AsyncPutImage", "ObjectNull");
            return;
        }

        File file = new File(localFile);
        if (!file.exists()) {
            Log.w("AsyncPutImage", "FileNotExist");
            Log.w("LocalFile", localFile);
            return;
        }

        // 构造上传请求
        final PutObjectRequest put = new PutObjectRequest(bucket, object, localFile);

        if (CALLBACK_SERVER != null) {
            put.setCallbackParam(new HashMap<String, String>() {
                {
                    put("callbackUrl", CALLBACK_SERVER);
                    put("callbackBody", "filename=${object}&mimeType=${mimeType}" +  "&ossType=${x:ossType}");
                    put("callbackBodyType", "application/json");

                }
            });
            put.setCallbackVars(new HashMap<String, String>() {
                {
                    put("x:ossType", ossType);// oss文件上传类型
                }
            });
        }

        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                // Log.d("PutObject", "currentSize: " + currentSize +
                // " totalSize: " + totalSize);
                // int progress = (int) (100 * currentSize / totalSize);
                // UIDisplayer.updateProgress(progress);
                // UIDisplayer.displayInfo("上传进度: " + String.valueOf(progress) +
                // "%");
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");

                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());

                // UIDisplayer.uploadComplete();
                // UIDisplayer.displayInfo("Bucket: " + bucket
                // + "\nObject: " + request.getObjectKey()
                // + "\nETag: " + result.getETag()
                // + "\nRequestId: " + result.getRequestId()
                // + "\nCallback: " +
                // result.getServerCallbackReturnBody());
                Log.i("upload success",
                        "Bucket: " + bucket + "\nObject: " + request.getObjectKey() + "\nETag: " + result.getETag()
                                + "\nRequestId: " + result.getRequestId() + "\nCallback: "
                                + result.getServerCallbackReturnBody());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion,
                                  ServiceException serviceException) {
                String info = "";
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                    info = clientExcepion.toString();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                    info = serviceException.toString();
                }
                // UIDisplayer.uploadFail(info);
                // UIDisplayer.displayInfo(info);
                Log.d("upload failure", info);
            }
        });
    }

    // OSS异步上传文件
    public static int imgCount = 0;
    public static final int OSS_FAIL_CLIENT = 1000;
    static OSSAsyncTask task = null;
    public static void asyncPutFile(OSS oss, final Handler handler, final int number, final String bucket, final String object, String localFile, final int which) {
        /*if (object.equals("")) return;
        File file = new File(localFile);
        if (!file.exists()) return;*/

        // 构造上传请求
        final PutObjectRequest put = new PutObjectRequest(bucket, object, localFile);

        if (!TextUtils.isEmpty(CALLBACK_SERVER)) {
            // 传入对应的上传回调参数，这里默认使用OSS提供的公共测试回调服务器地址
            put.setCallbackParam(new HashMap<String, String>() {
                {
                    put("callbackUrl", CALLBACK_SERVER);
                    // put("callbackHost", hostaddress);
                    // callbackBody可以自定义传入的信息
                    switch (number) {
                        case 0:
                            //相册
                            put("callbackBody", "imageUrl=${object}&mimeType=image&userId=${x:userId}");
                            break;
                        case 5:
                            //相册--(最后一张)
                            put("callbackBody", "imageUrl=${object}&mimeType=image&userId=${x:userId}");
                            break;
                        case 9:
                            //相册某一张
                            put("callbackBody", "imageUrl=${object}&mimeType=user_album&userId=${x:userId}&number=${x:number}");
                            break;
                        case 7:
                            //相册--(4张)
                            put("callbackBody", "imageUrl=${object}&mimeType=image&userId=${x:userId}");
                            break;
                        case 1:
                            //视频
                            put("callbackBody", "videoUrl=${object}&mimeType=video&userId=${x:userId}");
                            break;
                        case 2:
                            //语音
                            put("callbackBody", "audioUrl=${object}&mimeType=audio&userId=${x:userId}");
                            break;
                        case 3:
                            //认证的头像
                            put("callbackBody", "imageUrl=${object}&mimeType=headimage&userId=${x:userId}");
                            break;
                        case 6:
                            //头像
                            put("callbackBody", "imageUrl=${object}&mimeType=head_image&userId=${x:userId}");
                            break;
                        case 8:
                            //头像
                            put("callbackBody", "imageUrl=${object}&mimeType=head_image&userId=${x:userId}");
                            break;
                        case 4:
                            //背景
                            put("callbackBody", "imageUrl=${object}&mimeType=bgm_image&userId=${x:userId}");
                            break;
                        //上传举报部分
                        case 10:
                            put("callbackBody", "imageUrl=${object}&mimeType=report&type=${x:type}&text=${x:text}&userId=${x:userId}&anchordId=${x:anchordId}&time=${x:time}");
                            break;
                    }
                    // "callbackBodyType":"application/json"
                    // "application/x-www-form-urlencoded"
                    put("callbackBodyType", "application/json");
                }
            });
            put.setCallbackVars(new HashMap<String, String>() {
                {
                    //ConstString.updateUserData();
                    JSONObject jsonObject = JSON.parseObject(ConstString.user);
                    if (jsonObject != null) {
                        String userId = "";
                        if (ConstString.isLiver) {
                            userId = jsonObject.getString("anchorId");
                        } else {
                            userId = jsonObject.getString("userId");
                        }
                        put("x:userId", userId);
                        if (number == 9) put("x:number", String.valueOf(which));
                        if (number == 10) {
                            SharedPreferences sp = DemoApplication.context.getSharedPreferences("report", Context.MODE_PRIVATE);
                            //举报原因类型
                            put("x:type", sp.getString("type", String.valueOf(-1)));
                            //举报附加文字
                            put("x:text", sp.getString("text", ""));
                            //举报截图地址
                            //put("x:path", sp.getString("path", ""));
                            //举报用户
                            //put("x:userId", ConstString.userId);
                            //被举报的主播
                            put("x:anchordId", sp.getString("anchordId", ""));
                            //举报时间
                            put("x:time", sp.getString("time", ""));
                        }
                    }
                }
            });
        }

        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                 int progress = (int) (100 * currentSize / totalSize);
                switch (number) {
                    case 6:HeadActivity.img_progress = progress;
                    handler.sendEmptyMessage(HeadActivity.PROGRESS_HEAD);
                        break;
                    case 9:AlbumActivity.img_progress = progress;
                    handler.sendEmptyMessage(AlbumActivity.PROGRESS_ALBUM);
                        break;
                }
            }
        });

        task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                if (number == 0 || number == 3) imgCount++;
                if ((number == 0 || number == 3) && imgCount == 6) {
                    //handler.sendEmptyMessage(BecomeAnchorActivity.IMG_SUCCESS);
                    //imgCount = 0;
                }
                //if (imgCount >= 6) handler.sendEmptyMessage(BecomeAnchorActivity.IMG_SUCCESS);
                //if (number == 1) handler.sendEmptyMessage(BecomeAnchorActivity.VEDIO_SUCCESS);
                //if (number == 2) handler.sendEmptyMessage(BecomeAnchorActivity.AUDIO_SUCCESS);

                Log.d("OSS", "=======================SUCCESS============================");

                if (number == 6) handler.sendEmptyMessage(HeadActivity.SUCCESS);
                //if (number == 5) handler.sendEmptyMessage(ChangeAlbumActivity.SUCCESS);
                if (number == 4) handler.sendEmptyMessage(MeActivity.SUCCESS);
                if (number == 9) handler.sendEmptyMessage(AlbumActivity.SUCCESS);
                if (number == 10) handler.sendEmptyMessage(ReportActivity.SUCCESS);
                Log.d("PutObject", "UploadSuccess");

                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());

                Log.i("upload success",
                        "Bucket: " + bucket + "\nObject: " + request.getObjectKey() + "\nETag: " + result.getETag()
                                + "\nRequestId: " + result.getRequestId() + "\nCallback: "
                                + result.getServerCallbackReturnBody());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion,
                                  ServiceException serviceException) {
                Log.d("OSS", "========================FAIL===========================");
                if (number == 0 || number == 3) {
                    //handler.sendEmptyMessage(BecomeAnchorActivity.IMG_FAIL);
                    imgCount = 0;
                }
                //if (number == 1) handler.sendEmptyMessage(BecomeAnchorActivity.VEDIO_FAIL);
                //if (number == 2) handler.sendEmptyMessage(BecomeAnchorActivity.AUDIO_FAIL);


                if (number == 6) handler.sendEmptyMessage(HeadActivity.FAIL);
                //if (number == 7 || number == 5) handler.sendEmptyMessage(ChangeAlbumActivity.FAIL);
                if (number == 4) handler.sendEmptyMessage(MeActivity.FAIL);
                if (number == 9) handler.sendEmptyMessage(AlbumActivity.FAIL);
                if (number == 10) handler.sendEmptyMessage(ReportActivity.FAIL);
                //if (number == 2) handler.sendEmptyMessage(1);
                String info = "";
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                    info = clientExcepion.toString();
                    handler.sendEmptyMessage(OSS_FAIL_CLIENT);
                    //task.cancel();
                }
                if (serviceException != null) {
                    handler.sendEmptyMessage(OSS_FAIL_CLIENT);
                    Log.e("Err", serviceException.toString());
                            // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                    info = serviceException.toString();
                }
                Log.d("upload failure", info);
            }
        });
    }

    // 网址下载文件
    public static boolean downloadFile(String url, String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            URL myUrl = new URL(url);
            URLConnection conn = myUrl.openConnection();
            conn.connect();
            is = conn.getInputStream();
            fos = new FileOutputStream(file);
            // 把数据存入路径+文件名
            byte buf[] = new byte[1024];
            int numread = 0;
            while ((numread = is.read(buf)) != -1) {
                fos.write(buf, 0, numread);
            }
            return true;
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // OSS同步下载文件 一般对于private文件采用
    public static String downloadOssFileSynchronize(OSS oss, String bucketName, String objectKey, String localSaveDir) {
        // 构造下载文件请求
        GetObjectRequest get = new GetObjectRequest(bucketName, objectKey);
        try {
            // 同步执行下载请求，返回结果
            GetObjectResult getResult = oss.getObject(get);
            Log.d("Content-Length", "" + getResult.getContentLength());
            // 获取文件输入流
            InputStream inputStream = getResult.getObjectContent();
            String dir = localSaveDir;
            String subPath = objectKey;
            if (objectKey.contains("/")) {
                dir += "/" + objectKey.substring(0, objectKey.lastIndexOf("/"));
                subPath = objectKey.substring(objectKey.lastIndexOf("/") + 1);
            }
            File filedir = new File(dir);
            if (!filedir.exists()) {
                filedir.mkdirs();
            }
            String filePath = dir + "/" + subPath;
            File fnew = new File(filePath);
            if (!fnew.exists()) {
                fnew.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(fnew, true);
            byte[] buffer = new byte[1024];
            while (true) {
                int len = inputStream.read(buffer);
                if (len < 0) {
                    break;
                }
                fos.write(buffer);
            }
            fos.flush();
            // 下载后可以查看文件元信息
            ObjectMetadata metadata = getResult.getMetadata();
            Log.d("ContentType", metadata.getContentType());
            return filePath;
        } catch (ClientException e) {
            // 本地异常如网络异常等
            e.printStackTrace();
        } catch (ServiceException e) {
            // 服务异常
            Log.e("RequestId", e.getRequestId());
            Log.e("ErrorCode", e.getErrorCode());
            Log.e("HostId", e.getHostId());
            Log.e("RawMessage", e.getRawMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // OSS异步下载文件 一般对于private文件采用
    public static void downloadOssFileAsync(OSS oss, final String bucketName, final String objectKey,
                                            final String localSaveDir) {
        GetObjectRequest get = new GetObjectRequest(bucketName, objectKey);
        get.setxOssProcess("image/sharpen,100");

        OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {

                // 请求成功
                InputStream inputStream = result.getObjectContent();
                String dir = localSaveDir;
                String subPath = objectKey;
                if (objectKey.contains("/"))
                    ;
                {
                    dir += "/" + objectKey.substring(0, objectKey.lastIndexOf("/"));
                    subPath = objectKey.substring(objectKey.lastIndexOf("/") + 1);
                }
                File filedir = new File(dir);
                if (!filedir.exists()) {
                    filedir.mkdirs();
                }
                String filePath = dir + "/" + subPath;
                File fnew = new File(filePath);
                if (!fnew.exists()) {
                    try {
                        fnew.createNewFile();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(fnew, true);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                byte[] buffer = new byte[1024];
                while (true) {
                    try {
                        int len = inputStream.read(buffer);
                        if (len < 0) {
                            break;
                        }
                        fos.write(buffer);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        break;
                    }

                }
                try {
                    fos.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion,
                                  ServiceException serviceException) {
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });

    }

}

