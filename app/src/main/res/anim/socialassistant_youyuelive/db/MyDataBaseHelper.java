package com.socialassistant_youyuelive.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.socialassistant_youyuelive.AV.AVChatAudio;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zjm on 2017/5/11.
 */

public class MyDataBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "MyDataBaseHelper";
    // 设计MyDataBaseHelper成单例设计模式
    private static MyDataBaseHelper instance;
    private static final String DATABASENAME = "yydb.db";
    private static final int DATABASEVERSION = 1;
    // 新建一张表，订单表
    private String[] initCreateTables = new String[]{
            "create table if not exists orders("
                    + "order_id integer primary key autoincrement not null,"
                    + "timestamp integer," // 时间戳（去掉后6位）
                    + "consumer varchar(64),"
                    + "anchor varchar(64),"
                    + "consume_time_seconds integer,"
                    + "consume_time_minutes integer,"
                    + "consume_balance_before integer,"
                    + "consume_money_total integer,"
                    + "consume_balance_after integer,"
                    + "userId varchar(16),"
                    + "anchorId varchar(16),"
                    + "chatType bit,"
                    + "is_send_success boolean DEFAULT 0)",// 0 未发送 1 发送成功 -1 不用发送
            //通知表 or 聊天记录表
            "create table notice(_id INTEGER PRIMARY KEY AUTOINCREMENT,type varchar(10), " +
                    "nickName varchar(40), faceUrl varchar(100), id varchar(30), recordId varchar(50), " +
                    "pay varchar(50), time long, message varchar(50), roleType varchar(10),readflag char(1),usermessage varchar(50),userfaceUrl varchar(100)," +
                    "talk char(1))"
    };
    public MyDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {// 在没有数据库的情况下才会执行
        // 在没有数据库的情况下，执行初始化的建表操作
        for (String createtable : initCreateTables){
            db.execSQL(createtable);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 在有数据库的情况下，改变数据库版本之后回调的方法
        switch (newVersion){
            case 2:
                break;
            default:
                break;
        }
    }

    public static MyDataBaseHelper getInstance(Context context){
        if (instance == null){
            instance = new MyDataBaseHelper(context, DATABASENAME, null, DATABASEVERSION);
        }
        return instance;
    }

    // 查询未返回未完成的第一个订单
    public Map<String, Object> getUnsendOrder(SQLiteDatabase db){
        Map<String, Object> order = null;
        Cursor cursor = db.rawQuery("select * from orders where is_send_success=?", new String[]{"0"});
        if (cursor != null){
            while (cursor.moveToNext()){
                String consumer = cursor.getString(cursor.getColumnIndex("consumer"));
                String anchor = cursor.getString(cursor.getColumnIndex("anchor"));
                String userId = cursor.getString(cursor.getColumnIndex("userId"));
                String anchorId = cursor.getString(cursor.getColumnIndex("anchorId"));
                long pay = cursor.getLong(cursor.getColumnIndex("consume_money_total"));
                long recordIdString = cursor.getLong(cursor.getColumnIndex("timestamp"));
                int chatType = cursor.getInt(cursor.getColumnIndex("chatType"));
                order = new HashMap<String, Object>();
                order.put("consumer", consumer);
                order.put("anchor", anchor);
                order.put("userId", userId);
                order.put("anchorId", anchorId);
                order.put("pay", Math.abs(pay));
                order.put("recordIdString", recordIdString);
                order.put("chatType", chatType);
                break;
            }
            cursor.close();
        }
        return order;
    }

    // 根据timeStamp更新订单状态
    public boolean updateUnsendOrder(SQLiteDatabase db, long timeStamp){
        boolean isSuccess = false;
        Cursor cursor = db.rawQuery("select * from orders where timestamp=?", new String[]{"" + timeStamp});
        if (cursor != null){
            while (cursor.moveToNext()){
                db.execSQL("update orders set is_send_success=? where timestamp=?", new String[]{"1", "" + timeStamp});
                isSuccess = true;
            }
            cursor.close();
        }
        return isSuccess;
    }

    // 根据timeStamp删除订单记录
    public boolean deleteSendOrder(SQLiteDatabase db, long timeStamp){
        boolean isSuccess = false;
        try {
            db.execSQL("delete from orders where timestamp=?", new String[]{"" + timeStamp});
            isSuccess = true;
        }catch (SQLException e){
            isSuccess = false;
        }
        return isSuccess;
    }

    // 插入一条订单数据
    public boolean insertOrderData(SQLiteDatabase db, Map params){
        boolean isSuccess;
        try {
            long timeStamp = (long) params.get("timeStamp");
            String consumer = (String) params.get("consumer");
            String anchor = (String) params.get("anchor");
            int seconds = (int) params.get("seconds");
            int minutes = (int) params.get("minutes");
            long account_balance_before = (long) params.get("account_balance_before");
            long consume = (long) params.get("consume");
            long account_balance = (long) params.get("account_balance");
            String userId = (String) params.get("userId");
            String anchorId = (String) params.get("anchorId");
            int chatType = (int) params.get("chatType");
            int isSendSuccess = (int) params.get("isSendSuccess");
            db.execSQL("insert into orders(timestamp,consumer,anchor,consume_time_seconds," +
                            "consume_time_minutes,consume_balance_before," +
                            "consume_money_total,consume_balance_after,userId,anchorId,chatType,is_send_success) values(?,?,?,?,?,?,?,?,?,?,?,?)"
                    , new Object[]{timeStamp, consumer, anchor, seconds, minutes, account_balance_before, consume, account_balance,userId,anchorId,chatType,isSendSuccess});
            isSuccess = true;
        }catch (Exception e){
            isSuccess = false;
        }
        return isSuccess;
    }
}
