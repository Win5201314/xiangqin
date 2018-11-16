package com.socialassistant_youyuelive.util;

import android.content.Intent;

import com.jerey.animationlib.AnimationHelper;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.activity.BaseActivity;

/**
 * Created by Administrator on 2017/11/8.
 */

public class AnimaUtils {

    //跳转到其他界面的动画
    public static void toOtherActivity(BaseActivity thisActivity, Intent intent, int viewId, int colorId) {
        AnimationHelper.startActivity(thisActivity, intent, thisActivity.findViewById(viewId), colorId);
    }
}
