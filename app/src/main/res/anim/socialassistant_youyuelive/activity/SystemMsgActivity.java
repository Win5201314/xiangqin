package com.socialassistant_youyuelive.activity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.socialassistant_youyuelive.R;
import com.socialassistant_youyuelive.adapter.OrdersAdapter;
import com.socialassistant_youyuelive.commomentity.AboutMoney;
import com.socialassistant_youyuelive.commomentity.Orders;
import com.socialassistant_youyuelive.db.MyDataBaseHelper;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class SystemMsgActivity extends BaseActivity {

    //数据源
    private ListView lv;
    //当数据库没有东西的时候显示这个
    private TextView tverror;
    //数据
    private List<Orders> mList = new ArrayList<>();
    //适配器
    public OrdersAdapter adapter;
    public MyDataBaseHelper mB;
    public SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_msg);
        initToolbar(R.id.toolbar, R.id.title, "系统通知");
        initView();
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

    private void initView() {
        lv = (ListView) findViewById(R.id.sys_lv);
        tverror = (TextView) findViewById(R.id.sys_error);
        adapter = new OrdersAdapter(this,mList);
        lv.setAdapter(adapter);
        SearchIDActivity.ItemAnimation(getApplicationContext());
        LayoutAnimationController controller1 = new LayoutAnimationController(SearchIDActivity.set, 1);
        lv.setLayoutAnimation(controller1);   //ListView 设置动画效果*/
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //Animation animation = AnimationUtils.loadAnimation(context, R.anim.en);
                //lv.startAnimation(animation);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        onrefresh();
    }

    private void onrefresh() {
//        try{
//            mList.clear();
//            Cursor cursor = db.query("notice",null,null,null,null,null,null);
//            //sdf2.setTimeZone(TimeZone.getTimeZone("GMT-6:00"));
//            if(cursor.getCount() != 0){
//                if(cursor.getColumnCount() != 0) {
//                    while (cursor.moveToNext()) {
//                        Orders orders = new Orders();
//                        orders.setTime(cursor.getString(cursor.getColumnIndex("time")));
//                        orders.setPay(cursor.getString(cursor.getColumnIndex("pay")));
//                        orders.setRecordId(cursor.getString(cursor.getColumnIndex("recordId")));
//                        orders.setType(cursor.getString(cursor.getColumnIndex("type")));
//                        if(orders.getType().equals("0")){
//                            continue;
//                        }
//                        mList.add(orders);
//                    }
//                    tverror.setVisibility(View.GONE);
//                    lv.setVisibility(View.VISIBLE);
//                }else {
//                    lv.setVisibility(View.GONE);
//                    tverror.setVisibility(View.VISIBLE);
//                }
//            }else {
//                lv.setVisibility(View.GONE);
//                tverror.setVisibility(View.VISIBLE);
//            }
//            //当查询完后游标关闭
//            cursor.close();
//            db.close();
//            adapter.notifyDataSetChanged();
//            lv.setSelection(adapter.getCount());
        /*}catch (ParseException e){
            e.printStackTrace();
        }*/
        List<AboutMoney> aboutMoneys = DataSupport.findAll(AboutMoney.class);
        //ShowToast.normalShow(SystemMsgActivity.this,aboutMoneys.size() + "",true);
        if(aboutMoneys.isEmpty()){
            lv.setVisibility(View.GONE);
            tverror.setVisibility(View.VISIBLE);
            return;
        }else{
            tverror.setVisibility(View.GONE);
            lv.setVisibility(View.VISIBLE);
        }
        for (AboutMoney aboutMoney : aboutMoneys){
            if (aboutMoney == null) continue;
            if (aboutMoney.getType() == null) continue;
            if(aboutMoney.getType().equals("0")) continue;
            Orders orders = new Orders();
            orders.setType(aboutMoney.getType());
            orders.setTime(aboutMoney.getTime());
            orders.setPay(aboutMoney.getPay());
            orders.setRecordId(aboutMoney.getRecordId());
            orders.setMessage(aboutMoney.getMessage());
            mList.add(orders);
        }
        adapter.notifyDataSetChanged();
        lv.setSelection(adapter.getCount());
    }

}
