package com.socialassistant_youyuelive.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.socialassistant_youyuelive.GerstureImageview.com.polites.android.GestureImageView;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.activity.AboutAnchordActivity;
import com.socialassistant_youyuelive.adapter.AnchorFollowAdapter;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.RoundProgressBar;
import com.socialassistant_youyuelive.commomentity.UserID;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.ShowToast;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

public class ListFragmentFollow extends BaseFragment implements AnchorFollowAdapter.MyItemClickListener {
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:{
                    anchorfollowAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    };
    //当关注列表请求失败,或者当前没有关注
    public TextView tverror;
    // 标志位，标志已经初始化完成。
    private boolean isFollowViewOver;
    //加载View;
    public RecyclerView recyclerView;
    //最后一个Item
    public int lastVisibleItem;
    //下拉刷新布局
    public SwipeRefreshLayout mSwipeRefreshLayout;
    //适配器
    public AnchorFollowAdapter anchorfollowAdapter;
    public String image1 = null;
    public String image2 = null;
    public String image3 = null;
    public String image4 = null;
    public String image5 = null;
    // 持有这个动画的引用，让他可以在动画执行中途取消
    //private Animator mCurrentAnimator;
    //private int mShortAnimationDuration;
    public List<UserID> anchorList = new ArrayList<>();
    private View view;
    public ListFragmentFollow() {
        // Required empty public constructor
    }
    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_follow, container, false);
        isFollowViewOver = true;
        initData();
        init(view);
        return view;
    }

    @Override
    public void initData() {

    }
    private LinearLayout care_ly;
    private void init(View view) {
        //只要为true 在这个地方会报异常----会返回
        if(!isFollowViewOver || isVisible) {
            return;
        }
        //当关注列表请求失败,或者当前没有关注
        tverror = (TextView) view.findViewById(R.id.follow_error);
        care_ly = (LinearLayout) view.findViewById(R.id.care_ly);
        //布局
        recyclerView = (RecyclerView) view.findViewById(R.id.follow_lv);
        //下拉刷新布局
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.follow_swiperefreshlayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.fense);
//        //给布局加横线
        recyclerView.addItemDecoration(new RecyclerViewDivider(
                getActivity(), LinearLayoutManager.VERTICAL, 10, getResources().getColor(R.color.fense)));
        //布局需要管理者
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        //适配器
        anchorfollowAdapter = new AnchorFollowAdapter(anchorList);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                onrefresh();
            }
        });
        anchorfollowAdapter.addAll(anchorList,true);
        recyclerView.setAdapter(anchorfollowAdapter);
        anchorfollowAdapter.setItemClickListener(this);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //清除焦点----可能不行
                if(newState == SCROLL_STATE_TOUCH_SCROLL){
                    View currentFocus = getActivity().getCurrentFocus();
                    if (currentFocus != null) {
                        currentFocus.clearFocus();
                    }
                }
                if(newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == anchorfollowAdapter.getItemCount()){
                    onrefresh();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                lastVisibleItem =layoutManager.findLastVisibleItemPosition();
            }
        });
    }
    //每个Item的点击事件
    @Override
    public void onItemClick(View view, int position) {
        if (anchorList.size() <= position) return;
        String[] anlum = anchorList.get(position).getAlbum().split(",");
        for (int i = 0;i < anlum.length;i++){
            switch (i){
                case 0:image1 = anlum[i];break;
                case 1:image2 = anlum[i];break;
                case 2:image3 = anlum[i];break;
                case 3:image4 = anlum[i];break;
                case 4:image5 = anlum[i];break;
            }
        }
        switch (view.getId()){
            case R.id.follow_anchor_image_1:showBigImage(getActivity(), image1, 1);break;
            case R.id.follow_anchor_image_2:showBigImage(getActivity(), image2, 2);break;
            case R.id.follow_anchor_image_3:showBigImage(getActivity(), image3, 3);break;
            case R.id.follow_anchor_image_4:showBigImage(getActivity(), image4, 4);break;
            case R.id.follow_anchor_image_5:showBigImage(getActivity(), image5, 5);break;
            case R.id.follow_anchor_item:{
                UserID userId = anchorList.get(position);
                Intent intent = new Intent(getActivity(), AboutAnchordActivity.class);
                intent.putExtra("user_info",userId);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                break;
            }
        }
    }

    public static void showGerstureImage(Context context, String imageurl, int i) {
        if (TextUtils.isEmpty(imageurl)) return;
        LayoutInflater inflater = LayoutInflater.from(context);
        View imgEntryView = inflater.inflate(R.layout.gersurelayout, null); // 加载自定义的布局文件
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        final com.socialassistant_youyuelive.GerstureImageview.com.polites.android.GestureImageView gestureImageView =
                (GestureImageView) imgEntryView.findViewById(R.id.image);
        //加载本地文件如项目中assets下文件
        File file = ImageLoader.getInstance().getDiskCache().get(imageurl);
        if (file != null) {
            /*if (file.length() > 100 * 1024) {
                ShowToast.normalShow(context, "图片太大，显示失败!", true);
                return;
            }*/
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                Animation animation;
                animation = AnimationUtils.loadAnimation(context, R.anim.en);
                gestureImageView.startAnimation(animation);
                gestureImageView.setImageBitmap(bitmap);
            }
        }
        dialog.setView(imgEntryView); // 自定义dialog
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);  //此处可以设置dialog显示的位置
        switch (i) {
            case 5:
                window.setWindowAnimations(R.style.mystyle_2);  //添加动画
                break;
            case 2:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 3:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 4:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 1:
                window.setWindowAnimations(R.style.mystyle_3);  //添加动画
                break;
        }
        dialog.show();
        // 点击布局文件（也可以理解为点击大图）后关闭dialog，这里的dialog不需要按钮
        imgEntryView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                dialog.cancel();
            }
        });
    }

    public static void showBigImage(Context context, String imageurl, int i) {
        if (TextUtils.isEmpty(imageurl)) return;
        LayoutInflater inflater = LayoutInflater.from(context);
        View imgEntryView = inflater.inflate(R.layout.dialog_photo_entry, null); // 加载自定义的布局文件
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        final RoundProgressBar progressbar = (RoundProgressBar) imgEntryView.findViewById(R.id.roundProgressBar);
        final PhotoView test_iv = (PhotoView) imgEntryView.findViewById(R.id.test_iv);
        final ImageView img = (ImageView)imgEntryView.findViewById(R.id.large_image);
        //加载本地文件如项目中assets下文件
        File file = ImageLoader.getInstance().getDiskCache().get(imageurl);
        if (file != null) {
            /*if (file.length() > 100 * 1024) {
                ShowToast.normalShow(context, "图片太大，显示失败!", true);
                return;
            }*/
            //加载本地文件
            Picasso.with(context).load(file).into(test_iv);
            progressbar.setVisibility(View.GONE);
            img.setVisibility(View.GONE);
            test_iv.setVisibility(View.VISIBLE);
            PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(test_iv);
            //设置缩放
            photoViewAttacher.setZoomable(true);
            /*Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                test_iv.setImageBitmap(bitmap);
                progressbar.setVisibility(View.GONE);
                img.setVisibility(View.GONE);
                test_iv.setVisibility(View.VISIBLE);
                PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(test_iv);
                //设置缩放
                photoViewAttacher.setZoomable(true);
            } else {
                progressbar.setVisibility(View.VISIBLE);
                img.setVisibility(View.VISIBLE);
                test_iv.setVisibility(View.GONE);
                Animation animation;
                animation = AnimationUtils.loadAnimation(context, R.anim.en);
                img.startAnimation(animation);
                if (TextUtils.isEmpty(imageurl)) {
                    progressbar.setVisibility(View.GONE);
                } else {
                    //显示图片的配置
                    DisplayImageOptions options = new DisplayImageOptions.Builder()
                            /*//*.showImageOnLoading(R.drawable.logo)
                            .showImageOnFail(R.drawable.logo)
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .bitmapConfig(Bitmap.Config.RGB_565)
                            .build();
                    ImageLoader.getInstance().displayImage(imageurl, img, options, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            //开始加载的时候执行
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            //加载失败的时候执行
                            progressbar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            //加载成功的时候执行
                            progressbar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                            //加载取消的时候执行
                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            //在这里更新 ProgressBar的进度信息
                            //设置进度条图片的总大小
                            progressbar.setMax(total);
                            // 设置当前加载进度
                            progressbar.setProgress(current);
                            if (current == total) {
                                progressbar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }*/
        }
        dialog.setView(imgEntryView); // 自定义dialog
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);  //此处可以设置dialog显示的位置
        switch (i) {
            case 5:
                window.setWindowAnimations(R.style.mystyle_2);  //添加动画
                break;
            case 2:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 3:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 4:
                window.setWindowAnimations(R.style.mystyle);  //添加动画
                break;
            case 1:
                window.setWindowAnimations(R.style.mystyle_3);  //添加动画
                break;
            }
        dialog.show();
        // 点击布局文件（也可以理解为点击大图）后关闭dialog，这里的dialog不需要按钮
        imgEntryView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View paramView) {
                    dialog.cancel();
                }
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        onrefresh();
    }

    /**
     * 刷新关注列表
     */
    private void onrefresh() {
        JSONObject jsonObject = JSON.parseObject(ConstString.user);
        if(jsonObject == null) return;
        if(ConstString.isLiver){
            String anchorId = jsonObject.getString("anchorId");
            getfollowanchor(anchorId);
        }else {
            String userid = jsonObject.getString("userId");
            Log.d("用户",userid + "用户Id");
            getfollowanchor(userid);
        }
    }

    private void getfollowanchor(String Id) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", Id);
        params.put("time", String.valueOf(System.currentTimeMillis()));
        HttpUtil.getFollowAnchor(new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if(s != null && !s.equals("")) {
                    JSONObject jsobject = JSON.parseObject(s);
                    if (jsobject == null) {
                        recyclerView.setVisibility(View.GONE);
                        tverror.setVisibility(View.VISIBLE);
                        care_ly.setVisibility(View.VISIBLE);
                        return;
                    }
                    //判断是否查询成功
                    boolean status = jsobject.getBooleanValue("status");
                    if (!status) {
                        recyclerView.setVisibility(View.GONE);
                        tverror.setVisibility(View.VISIBLE);
                        care_ly.setVisibility(View.VISIBLE);
                        return;
                    }
                    String values = jsobject.getString("values");
                    //values为空是因为当前没有关注过主播所以将liveview隐藏
                    if (values == null || values.equals("")) {
                        recyclerView.setVisibility(View.GONE);
                        tverror.setVisibility(View.VISIBLE);
                        care_ly.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        tverror.setVisibility(View.GONE);
                        care_ly.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    JSONArray array = JSON.parseArray(values);
                    if (array.size() > 0) {
                        anchorList.clear();
                    }
                    //加载主播
                    loadingAnchor(array);
                }else {
                    recyclerView.setVisibility(View.GONE);
                    tverror.setVisibility(View.VISIBLE);
                    care_ly.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                recyclerView.setVisibility(View.GONE);
                tverror.setVisibility(View.VISIBLE);
                care_ly.setVisibility(View.VISIBLE);
            }
        },params);
    }

    private void loadingAnchor(JSONArray array) {
        for (int i = 0;i < array.size();i++) {
            JSONObject jo = JSON.parseObject(array.get(i).toString());
            UserID userID = new UserID();
            //主播五张图片
            userID.setAlbum(jo.getString("album"));
            //主播昵称
            if(jo.containsKey("nickName")){
                userID.setNickName(jo.getString("nickName"));
            }else if (jo.containsKey("mobile")){
                userID.setNickName(jo.getString("mobile"));
            }
            //主播年龄
            if(jo.containsKey("age")){
                userID.setYears(jo.getIntValue("age"));
            }
            //主播头像
            userID.setFace_url(jo.getString("face_url"));
            //主播签名
            if(TextUtils.isEmpty(jo.getString("signature"))){
                userID.setSignature("该主播什么都没有留下");
            }else {
                userID.setSignature(jo.getString("signature"));
            }
            //主播关注量
            userID.setFriendsAccount(jo.getIntValue("friendsAccount"));
            //主播城市
            userID.setCity(jo.getString("city"));
            //主播省份
            userID.setProvince(jo.getString("province"));
            //主播手机号
            userID.setMobile(jo.getString("mobile"));
            //主播Id
            userID.setAnchorId(Integer.parseInt(jo.getString("anchorId")));
            userID.setLabels(jo.getString("labels"));
            userID.setVoiceUrl(jo.getString("voiceUrl"));
            if(jo.getBooleanValue("isMan")){
                userID.setMan(true);
            }else {
                userID.setMan(false);
            }
            //判断主播直播状态 （0视频中 1可视频 2免打扰）
            if (jo.getIntValue("chat_status") == 0) {
                userID.setChat_status(0);
            }else if (jo.getIntValue("chat_status") == 1){
                userID.setChat_status(1);
            }else {
                userID.setChat_status(2);
            }
            //获取主播视频地址
            if(jo.containsKey("headVedio")) userID.setHeadVedio(jo.getString("headVedio"));
            anchorList.add(userID);
        }
        handler.sendEmptyMessage(1);
    }
}
//recyclerView 分割线
class RecyclerViewDivider extends RecyclerView.ItemDecoration {
    private Paint mPaint;
    private Drawable mDivider;
    private int mDividerHeight = 2;//分割线高度，默认为1px
    private int mOrientation;//列表的方向：LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    /**
     * 默认分割线：高度为2px，颜色为灰色
     *
     * @param context
     * @param orientation 列表方向
     */
    public RecyclerViewDivider(Context context, int orientation) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请输入正确的参数！");
        }
        mOrientation = orientation;

        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    /**
     * 自定义分割线
     *
     * @param context
     * @param orientation 列表方向
     * @param drawableId  分割线图片
     */
    public RecyclerViewDivider(Context context, int orientation, int drawableId) {
        this(context, orientation);
        mDivider = ContextCompat.getDrawable(context, drawableId);
        mDividerHeight = mDivider.getIntrinsicHeight();
    }

    /**
     * 自定义分割线
     *
     * @param context
     * @param orientation   列表方向
     * @param dividerHeight 分割线高度
     * @param dividerColor  分割线颜色
     */
    public RecyclerViewDivider(Context context, int orientation, int dividerHeight, int dividerColor) {
        this(context, orientation);
        mDividerHeight = dividerHeight;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
    }


    //获取分割线尺寸
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0, 0, 0, mDividerHeight);
    }

    //绘制分割线
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    //绘制横向 item 分割线
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mDividerHeight;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    //绘制纵向 item 分割线
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mDividerHeight;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }
}
