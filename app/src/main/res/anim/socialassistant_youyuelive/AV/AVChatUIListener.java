package com.socialassistant_youyuelive.AV;

/**
 * 音视频界面操作
 */
public interface AVChatUIListener {
    void onHangUp(final int type);
    void onRefuse();
    void onReceive();
    void toggleMute();
    void toggleSpeaker();
    void toggleRecord();
    void toggleSilence();
    void switchCamera();
    void closeCamera();
}
