package com.socialassistant_youyuelive.wxapi;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.adapter.LoopPagerAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.activity.AboutAnchordActivity;
import com.socialassistant_youyuelive.activity.SearchIDActivity;
import com.socialassistant_youyuelive.adapter.SuqareAnchorAdapter;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.ProgressView;
import com.socialassistant_youyuelive.commomentity.UserID;
import com.socialassistant_youyuelive.fragments.BaseFragment;
import com.socialassistant_youyuelive.fragments.VideoFragment;
import com.socialassistant_youyuelive.lizi.ParticleView;
import com.socialassistant_youyuelive.util.HttpUtil;
import com.socialassistant_youyuelive.util.ShowToast;
import com.squareup.picasso.Picasso;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
import static com.socialassistant_youyuelive.activity.BecomeAnchorActivity.getSDPath;
import static com.socialassistant_youyuelive.activity.BecomeAnchorActivity.saveBitmapToFile;

import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemLongClickListener;

public class ListFragmentSquare extends BaseFragment implements View.OnClickListener,
        IWXAPIEventHandler, RadioGroup.OnCheckedChangeListener,
        SuqareAnchorAdapter.MyItemClickListener,
        OnMenuItemClickListener, OnMenuItemLongClickListener {
    //通知适配器更新数据
    public Handler pushhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //更新 新的数据
                case 1: {
                    if (!Userlist.isEmpty()) {
                        if (recycleradapter != null) {
                            recycleradapter.addAll(Userlist, true);
                            footer.setVisibility(View.INVISIBLE);
                        }
                    }
                    break;
                }
                //当滑动到最底下时 改变底布局的文字
                case 2: {
                    if(footer != null){
                        footer.setVisibility(View.VISIBLE);
                        progressView.setVisibility(View.GONE);
                        if (UnAnchor != null) {
                            if (isAdded()) {
                                UnAnchor.setText(getResources().getString(R.string.suqare_footer_view));
                            }
                        }
                    }
                    break;
                }
                //当没有开启定位时,用户点了附近 强制更换为推荐列表
                case 3: {
                    if(recycleradapter != null){
                        recycleradapter.clear();
                    }
                    break;
                }
            }
        }
    };
    private ContextMenuDialogFragment mMenuDialogFragment;

    private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.tool_bar_height));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(false);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(this);
        mMenuDialogFragment.setItemLongClickListener(this);
    }

    private List<MenuObject> getMenuObjects() {
        // You can use any [resource, bitmap, drawable, color] as image:
        // item.setResource(...)
        // item.setBitmap(...)
        // item.setDrawable(...)
        // item.setColor(...)
        // You can set image ScaleType:
        // item.setScaleType(ScaleType.FIT_XY)
        // You can use any [resource, drawable, color] as background:
        // item.setBgResource(...)
        // item.setBgDrawable(...)
        // item.setBgColor(...)
        // You can use any [color] as text color:
        // item.setTextColor(...)
        // You can set any [color] as divider color:
        // item.setDividerColor(...)

        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();
        close.setResource(R.drawable.icn_close);

        MenuObject send = new MenuObject("分享给好友");
        send.setResource(R.mipmap.sharewx);
        send.setMenuTextAppearanceStyle(R.style.TextViewStyle);

        MenuObject like = new MenuObject("分享到朋友圈");
        like.setResource(R.mipmap.sharepyq);
        like.setMenuTextAppearanceStyle(R.style.TextViewStyle);

        MenuObject QR = new MenuObject("二维码分享");
        QR.setResource(R.mipmap.shareqr);
        QR.setMenuTextAppearanceStyle(R.style.TextViewStyle);

        //系统分享
        MenuObject xt = new MenuObject("列表中选择");
        xt.setResource(R.mipmap.xt);
        xt.setMenuTextAppearanceStyle(R.style.TextViewStyle);
        /*Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.share_p);//R.drawable.icn_2);
        like.setBitmap(b);*/

        /*MenuObject addFr = new MenuObject("Add to friends");
        BitmapDrawable bd = new BitmapDrawable(getResources(),
                BitmapFactory.decodeResource(getResources(), R.drawable.icn_3));
        addFr.setDrawable(bd);

        MenuObject addFav = new MenuObject("Add to favorites");
        addFav.setResource(R.drawable.icn_4);*/

        /*MenuObject block = new MenuObject("Block user");
        block.setResource(R.drawable.icn_5);*/

        menuObjects.add(close);
        menuObjects.add(send);
        menuObjects.add(like);
        menuObjects.add(QR);
        menuObjects.add(xt);
        /*menuObjects.add(addFr);
        menuObjects.add(addFav);*/
        //menuObjects.add(block);
        return menuObjects;
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        switch (position) {
            case 1:
                if (isInstallWX()) {
                    share(0);
                } else {
                    ShowToast.show(context, R.string.wechat_not_installed, true);
                }
                break;
            case 2:
                if (isInstallWX()) {
                    share(1);
                } else {
                    ShowToast.show(context, R.string.wechat_not_installed, true);
                }
                break;
            case 3:
                showQR(ConstString.share_app_url, 1);
                break;
            case 4:
                shareSingleImage(ConstString.share_app_url);
                //shareText(share_app_url);
                break;
        }
        //Toast.makeText(getActivity(), "Clicked on position: " + position, Toast.LENGTH_SHORT).show();
    }

    //分享文字
    /*public void shareText(String text) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setType("text/plain");

        //设置分享列表的标题，并且每次都显示分享列表
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }*/

    //分享单张图片
    public void shareSingleImage(String text) {
        //带LOGO的二维码
        Bitmap bitmap = createImageAddLogo(text);
        String pathImage = getSDPath() + "/youyue/qr.png";
        try {
            saveBitmapToFile(bitmap, pathImage);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String imagePath = pathImage;//Environment.getExternalStorageDirectory() + File.separator + "test.jpg";
        //由文件得到uri
        Uri imageUri = Uri.fromFile(new File(imagePath));
        Log.d("share", "uri:" + imageUri);  //输出：file:///storage/emulated/0/test.jpg

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    private void showQR(String url_qr, int type) {
        final Dialog dialog = new Dialog(getActivity(), R.style.UpdateDialog);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_qr_share, null);
        dialog.setContentView(view);
        ImageView qr_image = (ImageView) view.findViewById(R.id.qr_image);
        qr_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        switch (type) {
            case 0: {
                //不带LOGO的二维码
                Bitmap bitmap = createImage(url_qr);
                if (bitmap != null) qr_image.setImageBitmap(bitmap);
                break;
            }
            case 1: {
                //带LOGO的二维码
                Bitmap bitmap = createImageAddLogo(url_qr);
                if (bitmap != null) qr_image.setImageBitmap(bitmap);
                break;
            }
        }
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);  //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.mystyle_4);  //添加动画
        dialog.show();
    }

    // 生成QR图
    private int QR_WIDTH = 600;
    private int QR_HEIGHT = 600;

    private Bitmap createImage(String URL_SHARE) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            String text = URL_SHARE;
            if (text == null || "".equals(text) || text.length() < 1) {
                return null;
            }

            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE,
                    QR_WIDTH, QR_HEIGHT);

            System.out.println("w:" + martix.getWidth() + "h:"
                    + martix.getHeight());

            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
                    Bitmap.Config.ARGB_8888);

            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 生成QR图(带LOGO)
    private Bitmap createImageAddLogo(String text) {
        try {
            // 需要引入core包
            QRCodeWriter writer = new QRCodeWriter();
            // 把输入的文本转为二维码
            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE,
                    QR_WIDTH, QR_HEIGHT);

            //图像数据转换，使用了矩阵转换
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            for (int y = 0; y < QR_HEIGHT; y++) {

                //下面这里按照二维码的算法，逐个生成二维码的图片，//两个for循环是图片横列扫描的结果
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;//黑色
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;//白色
                    }

                }
            }
            //------------------添加图片部分------------------//
            Bitmap logoBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.lg);
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
                    Bitmap.Config.ARGB_8888);

            //设置像素点
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);

            Canvas canvas = new Canvas(bitmap);
            //二维码
            canvas.drawBitmap(bitmap, 0, 0, null);
            //图片绘制在二维码中央，合成二维码图片
            canvas.drawBitmap(logoBmp, bitmap.getWidth() / 2
                    - logoBmp.getWidth() / 2, bitmap.getHeight()
                    / 2 - logoBmp.getHeight() / 2, null);
            //------------------添加logo部分------------------//
            //code.setImageBitmap(bitmap);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onMenuItemLongClick(View clickedView, int position) {
        //Toast.makeText(getActivity(), "Long clicked on position: " + position, Toast.LENGTH_SHORT).show();
    }

    // 标志位，标志已经初始化完成。
    private boolean isSquareViewOver;
    //IWXAPI是第三方APP与微信通信的接口
    private IWXAPI api;
    //---------------------------------NEW---------------------------------------------
    //private RollPagerView mRollViewPager;
    //轮播图片地址
    public static String[] bannerPictureURLs = new String[3];
    private Context context;
    //显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder()
                /*.showImageOnLoading(R.drawable.ic_stub)
                .showImageOnFail(R.drawable.ic_error)*/
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();


    //推荐  附近
    public RadioGroup radioGroup;
    public RadioButton btn_choose, btn_nearby;
    //搜索
    public ImageView IvSearch;
    private LinearLayout share_app;
    //数据源
    public List<UserID> Userlist = new ArrayList<>();
    //显示
    public RecyclerView recyclerView;
    //最后一个Item
    public int lastVisibleItem;
    //适配器
    public SuqareAnchorAdapter recycleradapter;
    //下拉刷新布局------推荐
    public SwipeRefreshLayout mSwipefreshlayout;
    //头布局
    public View header;
    //底布局
    public View footer;
    //底布局的文字
    public TextView UnAnchor;
    //预加载----作用于推荐主播
    public static int choosePage = 1;
    //预加载---作用于附近主播
    public static int nearPage = 1;
    ProgressView progressView;

    //private SwipeRefreshLayout mWaveSwipe;

    public ListFragmentSquare() {
    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_square, container, false);
        //代表view初始化完成
        isSquareViewOver = true;
        initMenuFragment();
        context = getActivity();
        initView(view);
        resgitToWX();
        particleAnimator = new ParticleView(context, 3000);
        particleAnimator.setOnAnimationListener(new ParticleView.OnAnimationListener() {
            @Override
            public void onAnimationStart(View view, Animator animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(View view, Animator animation) {

                Animation an = AnimationUtils.loadAnimation(context, R.anim.en);
                view.startAnimation(an);
                view.setVisibility(View.VISIBLE);
            }
        });
        return view;
    }

    /*private ViewPager pager;
    private int pagerPosition;
    ArrayList<String> vs;*/
    private static final String STATE_POSITION = "STATE_POSITION";

    @Override
    public void initData() {
    }

    private void initView(View view) {
        //Log.d("fragmentSquare","isVisible" +":"+ isVisible);
        //只要为true 在这个地方会报异常----会返回
        if (!isSquareViewOver || isVisible) return;
        //下拉刷新布局----推荐
        mSwipefreshlayout = (SwipeRefreshLayout) view.findViewById(R.id.suqare_refresh_choose_layout);
        //动画设置
        /*SearchIDActivity.animation(1200);
        LayoutAnimationController controller1 = new LayoutAnimationController(SearchIDActivity.set, 1);
        mSwipefreshlayout.setLayoutAnimation(controller1);   //ListView 设置动画效果*/
        mSwipefreshlayout.setColorSchemeResources(R.color.fense);
        //搜索
        IvSearch = (ImageView) view.findViewById(R.id.suqare_top_search);
        //分享
        share_app = (LinearLayout) view.findViewById(R.id.share_app);

        //推荐附近总按钮
        radioGroup = (RadioGroup) view.findViewById(R.id.suqare_radio_group);
        //推荐按钮
        btn_choose = (RadioButton) view.findViewById(R.id.suqare_btn_choose);
        //附近按钮
        btn_nearby = (RadioButton) view.findViewById(R.id.suqare_btn_nearby);
        recyclerView = (RecyclerView) view.findViewById(R.id.suqare_recyclerView);
        final GridLayoutManager mgridLayoutManager = new GridLayoutManager(getActivity(), 2);
        mgridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //这个是设置头布局为一列的时候
                /*return recycleradapter.getItemViewType(position) == recycleradapter.TYPE_HEADER
                        ? mgridLayoutManager.getSpanCount() : 1;*/
                //这个是设置头布局和底布局都为一列的时候
                return recycleradapter.isHeader(position) ? mgridLayoutManager.getSpanCount()
                        : (recycleradapter.isFooter(position) ? mgridLayoutManager.getSpanCount() : 1);
            }
        });
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int postion = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
                if (recycleradapter.isHeader(postion) || recycleradapter.isFooter(postion)) {
                    outRect.set(0, 0, 0, 5);
                } else {
                    outRect.set(1, 5, 1, 5);
                }
            }
        });
        recyclerView.setLayoutManager(mgridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recycleradapter = new SuqareAnchorAdapter();
        recycleradapter.addAll(Userlist, true);
        setHeader(recyclerView);
        //轮播-----------------------------------
        /*mRollViewPager = (RollPagerView) header.findViewById(R.id.roll_view_pager);
        mRollViewPager.setAnimationDurtion(500);   //设置切换时间
        String s1 = "";
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("banner", Context.MODE_PRIVATE);
        s1 = sharedPreferences.getString("banner", "");
        if (!TextUtils.isEmpty(s1)) bannerPictureURLs = s1.substring(0, s1.length() - 1).split(",");
        if (TextUtils.isEmpty(bannerPictureURLs[0])
                || TextUtils.isEmpty(bannerPictureURLs[1])
                || TextUtils.isEmpty(bannerPictureURLs[2])) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isEmpty", true);
            editor.commit();
            //ShowToast.normalShow(getActivity(), s1, true);
            //本地模式
            mRollViewPager.setAdapter(new TestLoopAdapter(mRollViewPager)); //设置适配器
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isEmpty", false);
            editor.commit();
            //服务器获取
            mRollViewPager.setAdapter(new TestLoopAdapter(mRollViewPager)); //设置适配器
        }
        mRollViewPager.setHintView(new ColorPointHintView(context, Color.WHITE,
                context.getResources().getColor(R.color.fense)));// 设置圆点指示器颜色
        // mRollViewPager.setHintView(new IconHintView(this, R.drawable.point_focus, R.drawable.point_normal));
        //-----------------------------------------------------------------------*/
        setFooter(recyclerView);
        progressView = (ProgressView) footer.findViewById(R.id.pv);
        UnAnchor = (TextView) footer.findViewById(R.id.suqare_unanchor);
        recyclerView.setAdapter(recycleradapter);
        btn_choose.setChecked(true);
        IvSearch.setOnClickListener(this);
        share_app.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(this);
        recycleradapter.setItemClickListener(this);
        mSwipefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (radioGroup != null) {
                    if (btn_choose.isChecked()) {
                        Userlist.clear();
                        choosePage = 1;
                        onrefresh(true, choosePage);
                    }
                    if (btn_nearby.isChecked()) {
                        Userlist.clear();
                        nearPage = 1;
                        onrefresh(false, nearPage);
                    }
                }
                mSwipefreshlayout.setRefreshing(false);
            }
        });
        //处理当前recyclerView已经滑动到最底下并且当前的ViewHolder最后一个布局已经显示出来时,触发上拉刷新
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //清除焦点----可能不行
                if (SCROLL_STATE_TOUCH_SCROLL == newState) {
                    View currentFocus = getActivity().getCurrentFocus();
                    if (currentFocus != null) {
                        currentFocus.clearFocus();
                    }
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == recycleradapter.getItemCount()) {
                    if (btn_choose.isChecked()) {
                        onrefresh(true, ++choosePage);
                    }
                    if (btn_nearby.isChecked()) {
                        onrefresh(false, ++nearPage);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                lastVisibleItem = mgridLayoutManager.findLastVisibleItemPosition();
            }
        });
        /*/////////////////////////////////////////
        mWaveSwipe = (WaveSwipeRefreshLayout) view.findViewById(R.id.wave_swipe);
        //设置小圆圈颜色
        mWaveSwipe.setColorSchemeColors(Color.WHITE, Color.YELLOW,Color.RED,Color.GREEN);
        //设置背景色
        //mWaveSwipe.setWaveColor(Color.argb(255,63,81,181));
        mWaveSwipe.setWaveColor(getActivity().getResources().getColor(R.color.like_fense));
        //设置刷新监听
        mWaveSwipe.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(radioGroup != null){
                    if(btn_choose.isChecked()){
                        Userlist.clear();
                        choosePage = 1;
                        onrefresh(true,choosePage);
                    }
                    if(btn_nearby.isChecked()){
                        Userlist.clear();
                        nearPage = 1;
                        onrefresh(false,nearPage);
                    }
                }
                mWaveSwipe.setRefreshing(false);
            }
        });*/
    }

    //加载头布局
    private void setHeader(RecyclerView view) {
        //header = LayoutInflater.from(getActivity()).inflate(R.layout.activity_banner, view, false);
        header = LayoutInflater.from(getActivity()).inflate(R.layout.header_video, view, false);
        try {
            VideoFragment newFragment =  VideoFragment.newInstance();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.pager, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        recycleradapter.setHeaderView(header);
    }

    //加载底布局
    private void setFooter(RecyclerView view) {
        footer = LayoutInflater.from(getActivity()).inflate(R.layout.suqare_footer_view, view, false);
        recycleradapter.setFooterView(footer);
    }

    private FragmentManager fragmentManager;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.share_app: {
                //share_();
                fragmentManager = getActivity().getSupportFragmentManager();
                if (fragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
                    mMenuDialogFragment.show(fragmentManager, ContextMenuDialogFragment.TAG);
                }
                //menuPop();
                break;
            }
            case R.id.suqare_top_search: {
                startActivity(new Intent(getActivity(), SearchIDActivity.class));
                break;
            }
        }
    }

    public void share_() {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        View view = factory.inflate(R.layout.share_app, null);
        ImageView f = (ImageView) view.findViewById(R.id.friends);
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInstallWX()) {
                    share(0);
                } else {
                    ShowToast.show(getActivity(), R.string.wechat_not_installed, true);
                }
            }
        });
        ImageView pyq = (ImageView) view.findViewById(R.id.pyq);
        pyq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInstallWX()) {
                    share(1);
                } else {
                    ShowToast.show(getActivity(), R.string.wechat_not_installed, true);
                }
            }
        });
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                /*.setTitle(getResources().getString(R.string.app_name))*/
                /*.setMessage("message")*/
                .setView(view)
                .create();
        Window window = dialog.getWindow();
        int marginLeft = 10;
        WindowManager.LayoutParams wmlp = window.getAttributes();
        wmlp.gravity = Gravity.LEFT | Gravity.TOP;
        wmlp.x = marginLeft;
        wmlp.y = 100;
        wmlp.width = 100; // 宽度
        wmlp.height = 100; // 高度
        wmlp.alpha = 0.5f;
        window.setAttributes(wmlp);
        //window.setGravity(Gravity.VERTICAL_GRAVITY_MASK);  //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.mystyle_2);  //添加动画
        dialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.share_menu, menu);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {

    }

    public void resgitToWX() {
        // 注册到微信列表
        api = WXAPIFactory.createWXAPI(getActivity(), ConstString.APP_ID, false);
        api.registerApp(ConstString.APP_ID);
        try {
            api.handleIntent(getActivity().getIntent(), this);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void share(int type) {
        //初始化一个WXWebpageObject对象，填写URL
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = ConstString.share_app_url;
        //用WXWebpageObject对象初始化一个WXMediaMessage对象，填写标题和描述
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = getResources().getString(R.string.app_name);
        msg.description = getResources().getString(R.string.app_share_title);
        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.mipmap.logo);
        msg.thumbData = bmpToByteArray(thumb, true);
        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        //transaction字段用于唯一标识一个请求
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        if (type == 0) req.scene = SendMessageToWX.Req.WXSceneSession;
        if (type == 1) req.scene = SendMessageToWX.Req.WXSceneTimeline;
        //调用api接口发送数据到微信
        api.sendReq(req);
    }

    //检测是否安装了微信软件
    private boolean isInstallWX() {
        // 获取手机已安装的所有应用package的信息(其中包括用户自己安装的，还有系统自带的)
        for (PackageInfo pack : getActivity().getPackageManager().getInstalledPackages(0))
            if (pack.packageName.equals("com.tencent.mm")) return true;
        return false;
    }

    public byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) bmp.recycle();
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 构造一个用于请求的唯一标识
     *
     * @param type 分享的内容类型
     * @return
     */
    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis())
                : type + System.currentTimeMillis();
    }


    //推荐,附近的人实现
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.suqare_btn_choose: {
                Userlist.clear();
                recycleradapter.clear();
                //加载数据
                choosePage = 1;
                onrefresh(true, choosePage);
                break;
            }
            case R.id.suqare_btn_nearby: {
                Userlist.clear();
                recycleradapter.clear();
                nearPage = 1;
                //ShowToast.normalShow(getActivity(),ConstString.lng + ":经度,纬度:" + ConstString.lat,true);
                onrefresh(false, nearPage);
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Userlist == null || Userlist.size() <= 0) {
            Userlist = new ArrayList<>();
            onrefresh(true, choosePage);
        }/*else {
            if(Userlist != null && Userlist.size() > 0){
                Userlist.clear();
            }
            if(btn_choose != null && btn_nearby != null){
                if(btn_choose.isChecked()){
                    onrefresh(true,choosePage);
                }else {
                    onrefresh(true,nearPage);
                }
            }
        }*/
    }

    //推荐or附近请求数据
    private void onrefresh(boolean isChooseorNearby, int page) {
        /*ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.clearMemoryCache();
        imageLoader.clearDiskCache();*/
        //true---推荐  false---附近
        //推荐和附近请求时 Userlist会增加不会清空数据所以这里要注意一下
        Log.d("请求", "在请求数据!!!!!!!!!!!!!!!!!!!!!!!!");
        try {
            if (isChooseorNearby) {
                HttpUtil.getChooseAnchor(new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (!TextUtils.isEmpty(s)) {
                            JSONObject jsonobject = JSON.parseObject(s);
                            if (jsonobject == null) return;
                            String state = jsonobject.getString("state");
                            if (state != null && state.equals("ok")) {
                                String userjson = jsonobject.getString("anchorinfo");
                                if (TextUtils.isEmpty(userjson)) return;
                                JSONArray jsonarray = JSON.parseArray(userjson);
                                //加载每个主播信息
                                loadingAnchor(jsonarray);
                                if (pushhandler != null) pushhandler.sendEmptyMessage(1);
                            } else if (state.equals("unknown")) {
                                if (pushhandler != null) pushhandler.sendEmptyMessage(2);
                            }
                        } else {
                            if (pushhandler != null) pushhandler.sendEmptyMessage(3);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (pushhandler != null) pushhandler.sendEmptyMessage(3);
                        if (error instanceof NetworkError) {
                            ShowToast.normalShow(context, "本地网络链接异常,请检查网络!", true);
                        } else if (error instanceof ServerError) {
                            ShowToast.normalShow(context, "服务器繁忙，请稍后重试!", true);
                        } else if (error instanceof AuthFailureError) {
                            ShowToast.normalShow(context, "本地秘钥与服务器不一致!\n请重新登录软件!", true);
                        } else if (error instanceof NoConnectionError) {
                            ShowToast.normalShow(context, "本地网络链接异常!", true);
                        } else if (error instanceof TimeoutError) {
                            ShowToast.normalShow(context, "访问服务器超时!", true);
                        } else {
                            ShowToast.show(context, R.string.access_fail, true);
                        }
                    }
                }, page);
            } else {
                HttpUtil.getNearbyAnchor(new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (!TextUtils.isEmpty(s)) {
                            JSONObject jsonobject = JSON.parseObject(s);
                            if (jsonobject == null) return;
                            //取state字段
                            String state = jsonobject.getString("state");
                            if (state != null && state.equals("ok")) {
                                //从state取anchorinfo(里面包含全部主播)
                                String userjson = jsonobject.getString("anchorinfo");
                                if (TextUtils.isEmpty(userjson)) return;
                                JSONArray jsonarray = JSON.parseArray(userjson);
                                //加载每个主播信息
                                loadingAnchor(jsonarray);
                                if (pushhandler != null) pushhandler.sendEmptyMessage(1);
                            } else if (state.equals("unknown")) {
                                if (pushhandler != null) pushhandler.sendEmptyMessage(2);
                            }
                        } else {
                            if (pushhandler != null) pushhandler.sendEmptyMessage(3);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (pushhandler != null) pushhandler.sendEmptyMessage(3);
                        if (error instanceof NetworkError) {
                            ShowToast.normalShow(context, "本地网络链接异常,请检查网络!", true);
                        } else if (error instanceof ServerError) {
                            ShowToast.normalShow(context, "服务器繁忙，请稍后重试!", true);
                        } else if (error instanceof AuthFailureError) {
                            ShowToast.normalShow(context, "本地秘钥与服务器不一致!\n请重新登录软件!", true);
                        } else if (error instanceof NoConnectionError) {
                            ShowToast.normalShow(context, "本地网络链接异常!", true);
                        } else if (error instanceof TimeoutError) {
                            ShowToast.normalShow(context, "访问服务器超时!", true);
                        } else {
                            ShowToast.show(context, R.string.access_fail, true);
                        }
                    }
                }, ConstString.lng, ConstString.lat, page);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ListFragmentSquare", "");
        }
    }

    /**
     * 加载每个主播的数据
     *
     * @param
     * @param jsonarray
     */
    private void loadingAnchor(JSONArray jsonarray) {
        for (int i = 0; i < jsonarray.size(); i++) {
            JSONObject jo = JSON.parseObject(jsonarray.get(i).toString());
                            /*头像      ：headImgUrl
                              聊天状态  ：chat_status（0视频中 1可视频 2免打扰）
                              昵称      ：nickName
                              签名      ：signature*/
            if (jo == null) return;
            UserID userID = new UserID();
            //获取5个头像,已逗号分隔
            if (jo.containsKey("album")) {
                userID.setAlbum(jo.getString("album"));
            }
            //主播头像
            if (jo.containsKey("headImgUrl")) {
                userID.setFace_url(jo.getString("headImgUrl"));
            }
            //主播昵称
            if (jo.containsKey("nickName")) {
                userID.setNickName(jo.getString("nickName"));
            } else if (jo.containsKey("mobile")) {
                userID.setNickName(jo.getString("mobile"));
            }
            //主播签名
            if (jo.containsKey("signature")) {
                userID.setSignature(jo.getString("signature"));
            }
            //主播点赞数量
            if (jo.containsKey("friendsAccount")) {
                userID.setFriendsAccount(jo.getIntValue("friendsAccount"));
            }
            //主播省份
            if (jo.containsKey("province")) {
                userID.setProvince(jo.getString("province"));
            }
            //主播城市
            if (jo.containsKey("city")) {
                userID.setCity(jo.getString("city"));
            }
            //主播ID
            if (jo.containsKey("anchorId")) {
                userID.setAnchorId(jo.getIntValue("anchorId"));
            }
            //主播手机号
            if (jo.getString("mobile") != null && !jo.getString("mobile").equals("")) {
                userID.setMobile(jo.getString("mobile"));
            }
            //主播tokenID
            if (jo.containsKey("tokenId")) {
                userID.setTokenId(jo.getString("tokenId"));
            }
            //主播的三个标签,已逗号形式分开
            if (jo.containsKey("labels")) {
                userID.setLabels(jo.getString("labels"));
            }
            //获取语音URL
            if (jo.containsKey("voiceUrl")) {
                userID.setVoiceUrl(jo.getString("voiceUrl"));
            }
            //男或女?
            if (jo.containsKey("sex")) {
                userID.setMan(jo.getBooleanValue("sex"));
            }
            //判断主播直播状态 （0视频中 1可视频 2免打扰）
            if (jo.containsKey("chat_status")) {
                userID.setChat_status(jo.getIntValue("chat_status"));
            }
            //获取主播的openid
            if (jo.getString("openId") != null && !jo.getString("openId").equals("")) {
                userID.setOpenid(jo.getString("openId"));
            }
            //获取跟主播的距离
            if (jo.get("distance") != null) {
                userID.setDistance(jo.getFloatValue("distance"));
            }
            //获取主播视频地址
            if (jo.containsKey("headVedio")) {
                userID.setHeadVedio(jo.getString("headVedio"));
            }
            Userlist.add(userID);
        }
    }

    /**
     * recycleview每个Item的点击事件
     *
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(View view, int position) {
        //在点击item的时候,Userlist的数据可能会为空,所以去刷新当前页面
        if (recyclerView.getScrollState() != 0) return;
        if (Userlist.isEmpty()) {
            if (btn_choose.isChecked()) {
                choosePage = 1;
                Userlist.clear();
                if (Userlist.isEmpty()) {
                    onrefresh(true, choosePage);
                }
            } else {
                nearPage = 1;
                Userlist.clear();
                if (Userlist.isEmpty()) {
                    onrefresh(false, nearPage);
                }
            }
        } else {
            //在这里获取数据的时候,注意position - 1,不然会出现下标越界
            if (Userlist.size() <= 0) return;
            int N = position - 1;
            if (N >= 0 && Userlist.size() > N) {
                UserID userID = Userlist.get(N);
                Intent intent = new Intent(getActivity(), AboutAnchordActivity.class);
                intent.putExtra("user_info", userID);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        }
    }

    /**
     * 当前fragment给销毁时,把页数改为1
     */
    @Override
    public void onDetach() {
        super.onDetach();
        choosePage = 1;
        nearPage = 1;
        //Userlist.clear();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    //////////////////////
    private ParticleView particleAnimator;

    private class TestLoopAdapter extends LoopPagerAdapter {
        //banner图片
        public int[] imageId = new int[]{R.mipmap.b1, R.mipmap.b2, R.mipmap.b3};
        private int count = imageId.length;  // banner上图片的数量

        public TestLoopAdapter(RollPagerView viewPager) {
            super(viewPager);
        }

        @Override
        public View getView(final ViewGroup container, int position) {
            final int picNo = position + 1;
            ImageView view = new ImageView(container.getContext());
            view.setImageResource(imageId[position]);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            //ShowToast.normalShow(getActivity(), bannerPictureURLs[position], true);
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("banner", Context.MODE_PRIVATE);
            if (sharedPreferences.getBoolean("isEmpty", false)) {
                //是空的就采用本地图片
            } else {
                //加载本地文件如项目中assets下文件
                //File file = ImageLoader.getInstance().getDiskCache().get(bannerPictureURLs[position]);
                //if (file.exists()) file.delete();
                //加载本地去掉下面一行即可
                if (!TextUtils.isEmpty(bannerPictureURLs[position]))
                    Picasso.with(getActivity()).load(bannerPictureURLs[position]).into(view);
                //String fileName = getSDPath() +"/A.txt";//以name存在目录中
                //WriteTxtFile(bannerPictureURLs[position], fileName);
            }
            // 点击事件
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Animation an = AnimationUtils.loadAnimation(context, R.anim.banner);
                    mRollViewPager.startAnimation(an);*/
                    particleAnimator.boom(v);
                    //Toast.makeText(context, "点击了第" + picNo + "张图片", Toast.LENGTH_SHORT).show();
                }

            });
            return view;
        }

        @Override
        public int getRealCount() {
            return count;
        }
    }
}
