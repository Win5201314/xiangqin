package com.socialassistant_youyuelive.commomentity;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.socialassistant_youyuelive.AV.md5.MD5;
import com.socialassistant_youyuelive.oss.OSSUtil;

import org.litepal.crud.DataSupport;

public class ConstString {
    public static final String IP = "https://m3.seorj.cn:";
    //public static final String IP = "https://m3.seorj.cn";
    //public static final String IP = "http://192.168.1.108:8080";
    //public static final String IP = "http://192.168.1.145:8080";

    //阿里云存储，当外网改变，这里做相应的改变(查看OSSUtil这个类前面地址)
    public static final String OSS_IP = "https://m3.seorj.cn";
    //分享网址
    public static final String share_app_url = "http://woyoufen.com/";
    //
    public static final String url_JPush_ID = ConstString.IP + "/video/user/addRegisterId";
    //软件升级提醒地址
    public static final String NEW_VERSION = ConstString.IP + "/video/user/checkAppVersion";
    public static String APK_URL = "";

    //广告条地址
    public static final String Banner_url = IP + "/video/user/getIndexUrl";
    //请求最新的token app_key 个人数据
    public static final String URL_KEY = IP + "/video/user/getTokenId";

    public static final String MAIN_ACTION = "MAIN_YOUYUE";
    //加密检验
    public static String API_KEY = "";
    public static String KEY = "";
    //OSS服务器上传
    public static String SIGN = OSSUtil.STSGET_SERVER + "?mobile=" + ConstString.mobile + "&sign=" + MD5.getStringMD5("youyue" + ConstString.mobile);
    //编辑资料专用
    public static final String URL_INFO = IP + "/video/user/updateUser";
    /*//极光推送ID
    public static String JPushID = "";*/
    //本账号个人所有信息Json字符串,需要什么数据就解析出想要的数据
    public static String user = "";
    //手机账号
    public static String mobile = "";
    //普通用户
    public static String userId = "";
    //主播
    public static String anchor_id = "";
    //昵称
    public static String nickName = "";
    //是否有新通知
    public static boolean isNewMsg = false;
    //判断是否登录成功
    public static boolean isLogined = false;
    //判断是否是主播
    public static boolean isLiver = false;
    //图片截取宽高
    public static final int WIDTH = 300;
    public static final int HEIGHT = 400;

    // 微信开放平台申请到的app_id
    public static final String APP_ID = "wx22e6a6ee7a6357cc";
    // 微信开放平台申请到的app_id对应的app_secret
    public static final String APP_SECRET = "cee36b5de5f4f426e0976f2e41fd780b";
    // 经纬度
    public static String lng = "";
    public static String lat = "";
    //缓存目录
    public static final String PATH_CACHE = "/com.socialassistant_youyuelive/youyue/Cache";
    public static final String PA = "/youyue";
    //语音存放目录
    public static final String PATH_AUDIO = "/com.socialassistant_youyuelive/youyue/audio";
    //图灵接口
    public static final String TULING_URL = "http://www.tuling123.com/openapi/api";
    //图灵key
    public static final String[] TULING_KEY = new String[]{"d0b0c72711114383a4df877b8cf7f36d"/*,
            "43fa664ee0e04d728dd70e98f80995af","ed5ca447bf8f474ca5c3aba356a00507"*/};

    public static void updateUserData() {
        UserData userData = DataSupport.findFirst(UserData.class);
        if (userData == null) return;
        ConstString.isLogined = userData.isLogined();
        if (ConstString.isLogined) {
            ConstString.user = userData.getUserData();
            ConstString.isLiver = userData.isLiver();
            ConstString.API_KEY = userData.getToken();
            ConstString.KEY = userData.getKey();
            ConstString.mobile = userData.getMobile();
            JSONObject jsonObject = JSON.parseObject(ConstString.user);
            if (jsonObject == null) return;
            String data = "";
            if (ConstString.isLiver) {
                data = jsonObject.getString("anchorId");
                ConstString.anchor_id = !TextUtils.isEmpty(data) ? data.trim() : "";
            } else {
                data = jsonObject.getString("userId");
                ConstString.userId = !TextUtils.isEmpty(data) ? data.trim() : "";
            }
        }
    }

    //判断是否为VIP账号
    public static boolean isVIP() {
        ConstString.updateUserData();
        JSONObject jsonObject = JSON.parseObject(ConstString.user);
        if (jsonObject != null) {
            //member 0代表非会员， 1代表会员
            String member = jsonObject.getString("member");
            if (!TextUtils.isEmpty(member) && member.contains("1")) return true;
        }
        return false;
    }

}
