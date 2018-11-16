package com.socialassistant_youyuelive.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.util.ShowToast;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/11/6.
 */

public class VoiceRecorderActivity extends BaseActivity {

    private Context context;
    @BindView(R.id.showTime) TextView show;
    @BindView(R.id.begin) Button begin;
    @BindView(R.id.end) Button end;

    private File mRecAudioFile;        // 录制的音频文件
    private File mRecAudioPath;        // 录制的音频文件路徑
    private MediaRecorder mMediaRecorder;// MediaRecorder对象
    private String strTempFile = "recaudio_";// 零时文件的前缀

    Timer timer;
    int number = 0;
    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case 1:
                    number++;
                    show.setText(number + "s");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        context = this;
        ButterKnife.bind(this);
        initToolbar(R.id.toolbar, R.id.title, "录音");
        begin.setEnabled(true);
        end.setEnabled(false);
        /* 检测是否存在SD卡 */
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            mRecAudioPath = Environment.getExternalStorageDirectory();// 得到SD卡得路径
        } else {
            ShowToast.normalShow(context, "没有SD卡", true);
        }
        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    /* ①Initial：实例化MediaRecorder对象 */
                    mMediaRecorder = new MediaRecorder();
                      /* ②setAudioSource/setVedioSource*/
                    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置麦克风
                      /* ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default
                       * THREE_GPP(3gp格式，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
                       * */
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                      /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default */
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                      /* ②设置输出文件的路径 */
                    try {
                        mRecAudioFile = File.createTempFile(strTempFile, ".amr", mRecAudioPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
                      /* ③准备 */
                    mMediaRecorder.prepare();
                      /* ④开始 */
                    mMediaRecorder.start();
                    begin.setEnabled(false);
                    begin.setText("正在录音,请开始说话吧!");
                    end.setEnabled(true);
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(1);
                        }
                    }, 0, 1000);       // timeTask
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mRecAudioFile != null) {
                      /* ⑤停止录音 */
                    mMediaRecorder.stop();
                    /* ⑥释放MediaRecorder */
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                    /* 按钮状态 */
                    begin.setEnabled(true);
                    end.setEnabled(false);
                    number = 0;
                    if (timer != null) timer.cancel();
                    //数据是使用Intent返回
                    Intent intent = new Intent();
                    //把返回数据存入Intent
                    intent.putExtra("path", mRecAudioFile.getAbsolutePath());
                    setResult(RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
                    finish();//此处一定要调用finish()方法
                }
            }
        });
    }

    public Toolbar initToolbar(int id, int titleId, String titleString) {
        Toolbar toolbar = (Toolbar) findViewById(id);
        //toolbar.setTitle("");
        TextView textView = (TextView) findViewById(titleId);
        textView.setText(titleString);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }

}
