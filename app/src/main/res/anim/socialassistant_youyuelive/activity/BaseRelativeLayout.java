package com.socialassistant_youyuelive.activity;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.socialassistant_youyuelive.R;


public class BaseRelativeLayout extends RelativeLayout {
    private Activity activity;
    int currentScreen  = LEFT_SCREEN;
    private static final int LEFT_SCREEN = 0;
    private static final int RIGHT_SCREEN = 1;
    private Scroller mScroller;		// 专门用模拟数据.

    public BaseRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, R.style.Transparent);
    }

    public BaseRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseRelativeLayout(Context context) {
        super(context);
        this.activity = (Activity)context;
        mScroller = new Scroller(context);
        //获得触发滑动事件的最短距离
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    int oldX;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//		System.out.println("width:"+	getWidth()+"height:"+getHeight());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldX = (int)event.getX();
                break;
            case MotionEvent.ACTION_UP:
                if(getScrollX()<0&&getScrollX()>-getMeasuredWidth()/2){
                    currentScreen = LEFT_SCREEN;
                }
                if(getScrollX()<=-getMeasuredWidth()/2&&getScrollX()>=-getMeasuredWidth()){
                    currentScreen = RIGHT_SCREEN;
                }
                scrollScreen();
                break;
            case MotionEvent.ACTION_MOVE:
                //移动后的坐标
                int moveX = (int)event.getX();
                //偏移量
                int deltaX  = (oldX-moveX);
                //滑动距离加上偏移量
                int newScrollX = getScrollX() + deltaX;// 获得当前屏幕x轴的偏移量
                if(newScrollX>0){
                    scrollTo(0, 0);
                }else if(newScrollX<=-getMeasuredWidth()){
                    scrollTo(-getMeasuredWidth(), 0);
                    activity.finish();
                }else{
                    scrollBy(deltaX, 0);
                }
                oldX = moveX;
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 根据currentScreen变量来切换屏幕的显示
     */
    private void scrollScreen() {
        int scrollX = getScrollX();
        int dx = 0;
        if(currentScreen == LEFT_SCREEN) {
            // 向左滑动
            dx = 0 - scrollX;
        } else if(currentScreen == RIGHT_SCREEN) {
            // 向右滑动
            dx = -getChildAt(0).getMeasuredWidth() - scrollX;
        }
        // 开始模拟数据了.
        mScroller.startScroll(scrollX, 0, dx, 0, Math.abs(dx) * 2);
        // 刷新当前控件, 会引起当前控件的重绘.
        invalidate(); // -> drawChild -> child.draw -> computeScroll()
    }

    /**
     * 更新当前屏幕的x轴的值
     * 如果mScroller类一直正在模拟数据. 想让computeScroll方法一直被调用, mScroller停止模拟时停止调用此方法
     */
    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()) { // true 代表当前正在模拟数据. false 已经停止模拟数据了
            scrollTo(mScroller.getCurrX(), 0);
            invalidate();	// 递归, 自己掉自己.
            if(getScrollX()<=-getMeasuredWidth()){
                activity.finish();
            }
        }
    }

    /**
     * 在此拦截事件，左右滑动自身消费点事件
     */

    private int scaledTouchSlop;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldX =  (int)ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getX();
                int diff = Math.abs(moveX - oldX);
                if(diff > scaledTouchSlop) {
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
