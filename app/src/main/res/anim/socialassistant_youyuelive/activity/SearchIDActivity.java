package com.socialassistant_youyuelive.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.adapter.SearchIdAdapter;
import com.socialassistant_youyuelive.commomentity.ActionBarColorManager;
import com.socialassistant_youyuelive.commomentity.ActivityCollector;
import com.socialassistant_youyuelive.commomentity.ConstString;
import com.socialassistant_youyuelive.commomentity.ElasticListView;
import com.socialassistant_youyuelive.commomentity.UserID;
import com.socialassistant_youyuelive.util.ShowToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchIDActivity extends LeftSlipActivity implements View.OnClickListener {

    private FrameLayout frameLayout, frameLayout2;
    private EditText ed1, search_ed;
    private TextView search_fail, search_cancel;
    private ElasticListView listview;
    public SearchIdAdapter adapter;
    private List<UserID> ids;
    private ProgressDialog progressDialog;
    private static final String url_ID = ConstString.IP + "/video/anchor/anchorSeach";
    public static AnimationSet set, set1;
    private Context context;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 0) progressDialog.dismiss();
            if (msg.what == 1) {
                if (ids.size() > 0) {
                    search_fail.setVisibility(View.GONE);
                    listview.setVisibility(View.VISIBLE);
                    adapter = new SearchIdAdapter(SearchIDActivity.this, ids);
                    listview.setAdapter(adapter);
                } else {
                    search_fail.setVisibility(View.VISIBLE);
                    listview.setVisibility(View.GONE);
                }
            }
        }
    };
    Animation animation_1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_id);
        ActivityCollector.addActivity(this);
        context = this;
        ActionBarColorManager.setColor(this, getResources().getColor(R.color.fense));
        frameLayout = (FrameLayout) findViewById(R.id.search_layout);
        frameLayout.setOnClickListener(this);
        frameLayout2 = (FrameLayout) findViewById(R.id.search_layout_2);
        frameLayout2.setOnClickListener(this);
        animation_(100);
        LayoutAnimationController controller = new LayoutAnimationController(set1, 1);
        frameLayout.setLayoutAnimation(controller);   //ListView 设置动画效果
        ed1 = (EditText) findViewById(R.id.ed1);
        ed1.setOnClickListener(this);
        search_ed = (EditText) findViewById(R.id.search_ed);
        search_fail = (TextView) findViewById(R.id.search_fail);
        search_cancel = (TextView) findViewById(R.id.search_cancel);
        search_cancel.setOnClickListener(this);
        listview = (ElasticListView) findViewById(R.id.listview);
        ids = new ArrayList<>();
        animation_1 = AnimationUtils.loadAnimation(context, R.anim.en);
        frameLayout.startAnimation(animation_1);
        animation(1200);
        LayoutAnimationController controller1 = new LayoutAnimationController(set, 1);
        listview.setLayoutAnimation(controller1);   //ListView 设置动画效果
        /*search_ed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Log.d("R", "-------------------------------------------------");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ID = search_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(ID)) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle(getResources().getString(R.string.searchId));
                    progressDialog.setMessage(getResources().getString(R.string.searching_id));
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    searchMSG(ID);
                }
                //Log.d("R", "##################################################");
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Log.d("R", "**********************************************");
            }
        });*/
        /*search_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String ID = search_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(ID)) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle(getResources().getString(R.string.searchId));
                    progressDialog.setMessage(getResources().getString(R.string.searching_id));
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    searchMSG(ID);
                }
                return true;
            }
        });*/
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchIDActivity.this, AboutAnchordActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("user_info",ids.get(position));
                intent.putExtras(mBundle);
                //intent.putExtra("flag", true);
                startActivity(intent);
            }
        });
        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                closeKeyboard();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    private void closeKeyboard() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void animation_(int time) {
        set1 = new AnimationSet(false);
        Animation animation = new AlphaAnimation(0,1);   //AlphaAnimation 控制渐变透明的动画效果
        animation.setDuration(time);     //动画时间毫秒数
        set1.addAnimation(animation);    //加入动画集合
        animation = new TranslateAnimation(1, 13, 10, 50);  //RotateAnimation  控制画面角度变化的动画效果
        animation.setDuration(time);
        set1.addAnimation(animation);
        animation = new RotateAnimation(30,10);    //TranslateAnimation  控制画面平移的动画效果
        animation.setDuration(time);
        set1.addAnimation(animation);
        animation = new ScaleAnimation(5,0,2,0);    //ScaleAnimation 控制尺寸伸缩的动画效果
        animation.setDuration(time);
        set1.addAnimation(animation);
    }

    public static void ItemAnimation(Context context) {
        set = new AnimationSet(false);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.en);
        set.addAnimation(animation);
    }

    public static void animation(int time) {
        set = new AnimationSet(false);
        Animation animation = new AlphaAnimation(0,1);   //AlphaAnimation 控制渐变透明的动画效果
        animation.setDuration(time);     //动画时间毫秒数
        set.addAnimation(animation);    //加入动画集合
        animation = new TranslateAnimation(1, 13, 10, 50);  //RotateAnimation  控制画面角度变化的动画效果
        animation.setDuration(time);
        set.addAnimation(animation);
        animation = new RotateAnimation(30,10);    //TranslateAnimation  控制画面平移的动画效果
        animation.setDuration(time);
        set.addAnimation(animation);
        animation = new ScaleAnimation(5,0,2,0);    //ScaleAnimation 控制尺寸伸缩的动画效果
        animation.setDuration(time);
        set.addAnimation(animation);
    }

    @Override
    public void onClick(View v) {
        LayoutAnimationController controller;
        switch (v.getId()) {
            case R.id.search_layout:
                frameLayout.setVisibility(View.GONE);
                search_ed.setText("");
                //controller = new LayoutAnimationController(set, 1);
                //frameLayout2.setLayoutAnimation(controller);   //ListView 设置动画效果
                frameLayout2.setVisibility(View.VISIBLE);
                frameLayout2.startAnimation(animation_1);
                search_ed.requestFocus();
                InputMethodManager imm = (InputMethodManager) search_ed.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                break;
            case R.id.ed1:
                frameLayout.setVisibility(View.GONE);
                search_ed.setText("");
                //controller = new LayoutAnimationController(set, 1);
                //frameLayout2.setLayoutAnimation(controller);   //ListView 设置动画效果
                frameLayout2.setVisibility(View.VISIBLE);
                frameLayout2.startAnimation(animation_1);
                search_ed.requestFocus();
                InputMethodManager imm1 = (InputMethodManager) search_ed.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm1.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                break;
            case R.id.search_cancel:
                String ID = search_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(ID)) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle(getResources().getString(R.string.searchId));
                    progressDialog.setMessage(getResources().getString(R.string.searching_id));
                    progressDialog.setCancelable(true);
                    progressDialog.show();
                    searchMSG(ID);
                }
                /*closeKeyboard();
                controller = new LayoutAnimationController(set1, 1);
                frameLayout.setLayoutAnimation(controller);   //ListView 设置动画效果
                frameLayout.setVisibility(View.VISIBLE);
                frameLayout.startAnimation(animation_1);
                frameLayout2.setVisibility(View.GONE);
                search_fail.setVisibility(View.GONE);
                listview.setVisibility(View.GONE);*/
                break;
        }
    }

    private void searchMSG(final String ID) {
            // TODO Auto-generated method stub
            RequestQueue mQueue = Volley.newRequestQueue(this);
            StringRequest postRequest = new StringRequest(
                    Request.Method.POST,
                    url_ID,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject = JSON.parseObject(response);
                            handler.sendEmptyMessage(0);
                            if (jsonObject == null) {
                                handler.sendEmptyMessage(1);
                                return;
                            }
                            String state = jsonObject.getString("state");
                            if (state != null && state.equals("ok")) {
                                JSONArray array = JSON.parseArray(jsonObject.getString("anchorinfo"));
                                if (array.size() < 1) return;
                                ids.clear();
                                for (int i=0; i < array.size();i++){
                                    UserID id = parseUser((JSONObject) array.get(i));
                                    if (id != null) ids.add(id);
                                }
                            } else {
                                ids.clear();
                            }
                           handler.sendEmptyMessage(1);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    handler.sendEmptyMessage(0);
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
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("ID", ID);
                    return params;
                }
            };
            mQueue.add(postRequest);
    }

    private UserID parseUser(JSONObject jo) {
        if (jo == null) return null;
        UserID id = new UserID();
        String d = "";
        d = jo.getString("headImgUrl");
        d = (d != null && !d.equals("")) ? d : "";
        id.setFace_url(d);
        id.setVoiceUrl(jo.getString("voiceUrl"));
        d = jo.getString("nickName");
        d = (d != null && !d.equals("")) ? d : "小空";
        id.setNickName(d);
        d = jo.getString("openId");
        d = (d != null && !d.equals("")) ? d : "";
        id.setOpenid(d);
        //性别      : sex(false女 true男)
        id.setMan(jo.getBooleanValue("sex"));
        String age = jo.getString("age");
        id.setLabels(jo.getString("labels"));
        id.setYears(Integer.parseInt((age != null && !age.trim().equals("") ) ? age : "23"));
        id.setSignature(jo.getString("signature"));
        //聊天状态  ：chat_status（0视频中 1可视频 2免打扰）
        id.setLive(jo.getInteger("chat_status") == 0);
        id.setChat_status(jo.getIntValue("chat_status"));
        id.setAlbum(jo.getString("album"));
        id.setAnchorId(jo.getIntValue("anchorId"));
        id.setAnchorLevel(jo.getIntValue("anchorLevel"));
        id.setCity(jo.getString("city"));
        id.setProvince(jo.getString("province"));
        id.setCountry(jo.getString("country"));
        id.setFriendsAccount(jo.getIntValue("friendsAccount"));
        id.setMobile(jo.getString("mobile"));
        id.setTokenId(jo.getString("tokenId"));
        id.setJPush_ID(jo.getString("JPush_ID"));
        //获取主播视频地址
        if(jo.containsKey("headVedio")) id.setHeadVedio(jo.getString("headVedio"));
        return id;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            //添加界面切换效果，注意只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
            int v1 = Integer.valueOf(android.os.Build.VERSION.SDK);
            if(v1 >= 5) {
                //此为自定义的动画效果
                overridePendingTransition(R.anim.en, R.anim.activity_exit);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

}
