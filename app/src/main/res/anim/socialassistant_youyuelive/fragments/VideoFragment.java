package com.socialassistant_youyuelive.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.ListGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.socialassistant_youyuelive.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/21.
 */

public class VideoFragment extends Fragment {

    public ListGSYVideoPlayer gsyVideoPlayer;

    public VideoFragment() {
    }

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        gsyVideoPlayer = (ListGSYVideoPlayer) view.findViewById(R.id.show);
        //增加封面
        //gsyVideoPlayer.setThumbImageView(holder.imageView);
        List<GSYVideoModel> model = setModelData();
        //model = null;
        //从服务器获取失败，则采用本地的
        /*if (model == null || model.size() <= 0) {
            model = new ArrayList<>();
            String s = "http://attachment-cdn-oss.s1.seorj.cn/video/6561/15029358373040.mp4";
            model.add(new GSYVideoModel(s, ""));
            model.add(new GSYVideoModel(s, ""));
            model.add(new GSYVideoModel(s, ""));
        }*/
        //设置播放url，第一个url，第二个开始缓存，第三个使用默认缓存路径，第四个设置title
        gsyVideoPlayer.setUp(model, false, 0);


        //非全屏下，不显示title
        // gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);
        //非全屏下不显示返回键
        // gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
        //打开非全屏下触摸效果
        gsyVideoPlayer.setIsTouchWiget(true);

        //设置显示比例，默认SCREEN_TYPE_DEFAULT ，自适应
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);
        //开启自动旋转
        //  gsyVideoPlayer.setRotateViewAuto(true);
        //全屏首先横屏
        //  gsyVideoPlayer.setLockLand(true);
        //是否需要全屏动画效果
        //   gsyVideoPlayer.setShowFullAnimation(true);
        //不需要非wifi状态提示
        //    gsyVideoPlayer.setNeedShowWifiTip(true);
        gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!gsyVideoPlayer.isIfCurrentIsFullscreen())
                    gsyVideoPlayer.startWindowFullscreen(getActivity(), true, true);
            }
        });

        //  gsyVideoPlayer.setLockLand(false);
        gsyVideoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //gsyVideoPlayer.backFromWindowFull(getActivity());
                getActivity().onBackPressed();
            }
        });
        gsyVideoPlayer.getStartButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gsyVideoPlayer.getCurrentState() == GSYVideoPlayer.CURRENT_STATE_PAUSE) {
                    try {
                        gsyVideoPlayer.onVideoResume();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        gsyVideoPlayer.requestFocus();
                        //立即播放
                        gsyVideoPlayer.startPlayLogic();
                    }
                } else if (gsyVideoPlayer.getCurrentState() == GSYVideoPlayer.CURRENT_STATE_PLAYING) {
                    gsyVideoPlayer.onVideoPause();
                } else {
                    gsyVideoPlayer.requestFocus();
                    //立即播放
                    gsyVideoPlayer.startPlayLogic();
                }

            }
        });
        // postponeEnterTransition();
        // ViewCompat.setTransitionName(videoPlayer, IMG_TRANSITION);
        //  addTransitionListener();
        // startPostponedEnterTransition();
        //gsyVideoPlayer.startPlayLogic();

        //增加封面
        // ImageView imageView = new ImageView(getActivity());
        //  imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //  gsyVideoPlayer.setThumbImageView(imageView);
        //  Picasso.with(getActivity()).load(firstImageUrl).into(imageView);
        gsyVideoPlayer.setThumbPlay(true);

        gsyVideoPlayer.requestFocus();
        //立即播放

        gsyVideoPlayer.startPlayLogic();


        //gsyVideoPlayer.showSmallVideo(new Point(300, 300), true, true);
        return view;
    }

    private List<GSYVideoModel> setModelData() {
        String s1 = "";
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("banner", Context.MODE_PRIVATE);
        s1 = sharedPreferences.getString("banner", "");
        String[] bannerPictureURLs;
        if (!TextUtils.isEmpty(s1)) bannerPictureURLs = s1.substring(0, s1.length() - 1).split(",");
        else return null;

        List<GSYVideoModel> m = new ArrayList<>();
        for (String s : bannerPictureURLs) {
            if (!TextUtils.isEmpty(s)) m.add(new GSYVideoModel(s, ""));
        }

        if (m != null && m.size() > 0) return m;
        return null;
    }


//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private boolean addTransitionListener() {
//        transition = getWindow().getSharedElementEnterTransition();
//        if (transition != null) {
//            transition.addListener(new OnTransitionListener(){
//                @Override
//                public void onTransitionEnd(Transition transition) {
//                    super.onTransitionEnd(transition);
//                    videoPlayer.startPlayLogic();
//                    transition.removeListener(this);
//                }
//            });
//            return true;
//        }
//        return false;
//    }

    @Override
    public void onPause() {
        super.onPause();
        gsyVideoPlayer.onVideoPause();
        gsyVideoPlayer.release();
        gsyVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        gsyVideoPlayer.releaseAllVideos();
    }

}
