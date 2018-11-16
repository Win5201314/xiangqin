package com.socialassistant_youyuelive.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {
    //根部view
    private View rootView;
    protected Context context;
    private Boolean hasInitData = false;
    private boolean isPrepared;
    //控制是否显示
    protected boolean isVisible;
    //是否第一次运行,确保initData()方法只执行一次
    private boolean isFirst = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            if (rootView == null) {
                rootView = initView(inflater,container,savedInstanceState);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return rootView;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(rootView == null){
            return;
        }
        if(getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    private void onInvisible() {
    }

    private void onVisible() {
        if (!isPrepared || !isVisible || !isFirst) {
            return;
        }
        initData();
        isFirst = false;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!hasInitData) {
            initData();
            hasInitData = true;
        }
    }
   @Override
    public void onDestroyView() {
        super.onDestroyView();
      //((ViewGroup) rootView.getParent()).removeView(rootView);
    }
    /**
     * 子类实现初始化View操作
     */
    protected abstract View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * 子类实现初始化数据操作(子类自己调用)
     */
    public abstract void initData();

   /* *//**
     * 封装从网络下载数据
     *//*
    protected void loadData(HttpRequest.HttpMethod method, String url,
                            RequestParams params, RequestCallBack<String> callback) {
        if (0 == NetUtils.isNetworkAvailable(getActivity())) {
            new CustomToast(getActivity(), "无网络，请检查网络连接！", 0).show();
        } else {
            NetUtils.loadData(method, url, params, callback);
        }
    }*/
}
