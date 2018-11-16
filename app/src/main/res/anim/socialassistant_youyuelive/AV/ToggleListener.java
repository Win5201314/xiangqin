package com.socialassistant_youyuelive.AV;

import android.view.View;

/**
* 创建按钮按压的接口
*/
public interface ToggleListener {
    void toggleOn(View v);
    void toggleOff(View v);
    void toggleDisable(View v);
}
