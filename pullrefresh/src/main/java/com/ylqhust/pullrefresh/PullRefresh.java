package com.ylqhust.pullrefresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.telecom.Call;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;


/**
 * Created by ylqhust on 15/12/31.
 */
public class PullRefresh extends LinearLayout{

//    任务还未开始
    private final static int TASK_NOT_BEGIN = 0x01;
//    任务准备
    private final static int TASK_READY = 0x02;
//    任务进行中
    private final static int TASK_ING = 0x03;
//    任务完成成功
    private final static int TASK_END_SUCCESS = 0x04;
//    任务完成失败
    private final static int TASK_END_FAILED = 0x05;


//    记录当前头部任务状态
    private int mCurrentHeaderTask = TASK_NOT_BEGIN;
//    记录当前尾部任务状态
    private int mCurrentFooterTask = TASK_NOT_BEGIN;

//    完全隐藏
    private final static int STATUS_COMPLETE_HIDE = 0x01;
//    部分显示
    private final static int STATUS_PART_SHOW = 0x02;
//    完全显示
    private final static int STATUS_COMPLETE_SHOW = 0x03;
//    超过完全显示
    private final static int STATUS_OVER_COMPLETE_SHOW = 0x04;
//    记录当前头部状态
    private int mCurrentHeaderStatus = STATUS_COMPLETE_HIDE;
//    记录当前尾部状态
    private int mCurrentFooterStatus = STATUS_COMPLETE_HIDE;

    private View mHeader;
    private HeaderHolder mHeaderHolder;
    private MarginLayoutParams mHeaderMLP;
    private int mHeaderInitTopMargin;
    private View mMidder;
    private MarginLayoutParams mMidderMLP;
    private View mFooter;
    private FooterHolder mFooterHolder;
    private MarginLayoutParams mFooterMLP;
    private int mFooterInitTopMargin;

    private Task task;//上拉下拉刷新任务
    private CallBack callBack;

    public PullRefresh(Context context) {
        super(context);
    }

    public PullRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullRefresh(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PullRefresh(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void onLayout(boolean changed,int a,int b,int c,int d){
        super.onLayout(changed, a, b, c, d);
         if (!loadOnce) {
             initBaseData();
             loadOnce = true;
         }
    }


    /**
     * 初始化一些基本的界面数据
     */
    private void initBaseData() {
        if (mHeader != null){
            mHeaderMLP = (MarginLayoutParams) mHeader.getLayoutParams();
            mHeaderInitTopMargin = mHeaderMLP.topMargin;
        }
        mMidderMLP = (MarginLayoutParams) mMidder.getLayoutParams();
        if (mFooter != null){
            mFooterMLP = (MarginLayoutParams) mFooter.getLayoutParams();
            mFooterInitTopMargin = mFooterMLP.topMargin;
        }
    }

    private float mYDown;//当手指按下的时候的y的坐标
    private boolean mIsDown = false;//判断手指是否按下了
    private int mHeaderTopMarginWhenDown = mHeaderInitTopMargin;
    private int mFooterTopMarginWhenDown = mFooterInitTopMargin;
    private boolean loadOnce = false;
    private boolean mAllowTouch = true;//是否允许触摸
    private boolean mFirstTouch = true;//判断是否是第一次触碰屏幕




    private void attachTouch() {
        mMidder.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mAllowTouch)
                    return true;
                boolean top = callBack.CanotPullDown();
                boolean bottom = callBack.CanotPullUp();
                if (top || shouldIntercept() || bottom) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            if (mFirstTouch) {
                                //将第一次ACTION_MOVE视作ACTION_DOWN处理，因为发现ListView中的item如果
//                               设置了OnClickListener，那么listView不会接受到ACTION_DOWN事件，所以只能把
//                                第一次ACTION_MOVE视作ACTION_DOWN处理，以保证处理的合理性
//                                这样做也是可以的，因为ACTION_DOWN和第一次ACTION_MOVE之间相差不了多少
//                                这样做是完全可行的
                                mFirstTouch = false;
                            } else {
                                if (mIsDown) {
                                    int dy = (int) (event.getY() - mYDown);
                                    if (shouldIntercept()) {
                                        if (mCurrentHeaderStatus != STATUS_COMPLETE_HIDE && mHeader != null)
                                            MarginHeader(dy);
                                        else if (mCurrentFooterStatus != STATUS_COMPLETE_HIDE && mFooter != null)
                                            MarginFooter(dy);
                                        else
                                            return false;
                                    } else {
                                        if (top) {
                                            if (dy <= 0)
                                                return false;
                                            if (mHeader != null)
                                                MarginHeader(dy);
                                        } else if (bottom) {
                                            if (dy >= 0)
                                                return false;
                                            if (mFooter != null)
                                                MarginFooter(dy);
                                        }
                                    }
                                }
                                break;
                            }
                        case MotionEvent.ACTION_DOWN:
                            mYDown = event.getY();
                            mIsDown = true;
                            mHeaderTopMarginWhenDown = mHeaderMLP != null ? mHeaderMLP.topMargin : 0;
                            mFooterTopMarginWhenDown = mFooterMLP != null ? mFooterMLP.topMargin : 0;
                            return false;
                        case MotionEvent.ACTION_UP:
                            mIsDown = false;
                            ProcessActionUp();
                            mFirstTouch = true;
                            break;
                    }
                    if (mHeader != null) {
                        ChangeHeaderStatus();
                        ChangeHeaderUI();
                    }
                    if (mFooter != null) {
                        ChangeFooterStatus();
                        ChangeFooterUI();
                    }
                }
                if (shouldIntercept())
                    return true;
                return false;
            }
        });
    }


    private void ProcessActionUp() {
        //如果头部超出完全，那么就将头部设为完全显示
        if (mCurrentHeaderStatus == STATUS_OVER_COMPLETE_SHOW || mCurrentHeaderStatus == STATUS_COMPLETE_SHOW){
            ShowHeader();
            tryExecuteTask();
            tryHide();//有时候是完全显示的，但是当前task是not_begin,因此必须加上此步骤
            return;
        }
//        如果头部部分显示，那么就将头部隐藏
        if (mCurrentHeaderStatus == STATUS_PART_SHOW){
            HideHeader();
            return;
        }
//        如果尾部超出完全显示，那么就将尾部部分显示
        if(mCurrentFooterStatus == STATUS_COMPLETE_SHOW || mCurrentFooterStatus == STATUS_OVER_COMPLETE_SHOW){
            ShowFooter();
            tryExecuteTask();
            tryHide();//有时候是完全显示的，但是当前task是not_begin,因此必须加上此步骤
            return;
        }
//        如果尾部部分显示，就将尾部隐藏
        if(mCurrentFooterStatus == STATUS_PART_SHOW){
            HideFooter();
            return;
        }
    }

    private void tryHide() {
        if (mCurrentFooterTask == TASK_NOT_BEGIN && mCurrentFooterStatus != STATUS_COMPLETE_HIDE){
            System.out.println("tryHideFooter");
            HideFooter();
            return;
        }
        if (mCurrentHeaderTask == TASK_NOT_BEGIN && mCurrentHeaderStatus != STATUS_COMPLETE_HIDE){
            System.out.println("tryHideHeader");
            HideHeader();
            return;
        }
    }

    private void tryExecuteTask() {
        //如果任务标记为准备执行任务，就执行任务，同时设置任务标记
        if (mCurrentHeaderTask == TASK_READY && task != null){
            task.HeaderTask();
            mCurrentHeaderTask = TASK_ING;
        }
        //如果任务标记为准备执行任务，就执行任务，同时设置任务标记
        if (mCurrentFooterTask == TASK_READY && task != null){
            task.FooterTask();
            mCurrentFooterTask = TASK_ING;
        }
    }

    private void ShowHeader(){
        ValueAnimator valueAnimator = ValueAnimator.ofInt(mHeaderMLP.topMargin,mHeaderInitTopMargin + mHeaderHolder.getHeaderCompleteShowHeight());
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAllowTouch = false;
                int value = (int) animation.getAnimatedValue();
                mHeaderMLP.topMargin = value;
                mHeader.setLayoutParams(mHeaderMLP);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCurrentHeaderStatus = STATUS_COMPLETE_SHOW;
                mAllowTouch = true;
            }
        });
        valueAnimator.start();
    }
    private void HideHeader(){
        ValueAnimator valueAnimator = ValueAnimator.ofInt(mHeaderMLP.topMargin,mHeaderInitTopMargin);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAllowTouch = false;
                int value = (int) animation.getAnimatedValue();
                mHeaderMLP.topMargin = value;
                mHeader.setLayoutParams(mHeaderMLP);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCurrentHeaderStatus = STATUS_COMPLETE_HIDE;
                mAllowTouch = true;
            }
        });
        valueAnimator.start();
    }
    private void ShowFooter(){
        ValueAnimator valueAnimator = ValueAnimator.ofInt(mMidderMLP.topMargin,-mFooterHolder.getFooterCompleteShowHeight());
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAllowTouch = false;
                int value = (int) animation.getAnimatedValue();
                mMidderMLP.topMargin = value;
                mMidderMLP.bottomMargin = -mMidderMLP.topMargin;
                mFooterMLP.topMargin = mMidderMLP.topMargin;
                mMidder.setLayoutParams(mMidderMLP);
                mFooter.setLayoutParams(mFooterMLP);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCurrentFooterStatus = STATUS_COMPLETE_SHOW;
                mAllowTouch = true;
            }
        });
        valueAnimator.start();
    }
    private void HideFooter(){
        ValueAnimator valueAnimator = ValueAnimator.ofInt(mMidderMLP.topMargin,mFooterInitTopMargin);
        valueAnimator.setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAllowTouch = false;
                int value = (int) animation.getAnimatedValue();
                mMidderMLP.topMargin = value;
                mMidderMLP.bottomMargin = -mMidderMLP.topMargin;
                mFooterMLP.topMargin = mMidderMLP.topMargin;
                mMidder.setLayoutParams(mMidderMLP);
                mFooter.setLayoutParams(mFooterMLP);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCurrentFooterStatus = STATUS_COMPLETE_HIDE;
                mAllowTouch = true;
            }
        });
        valueAnimator.start();
    }


    private void ChangeFooterUI() {
        if (mCurrentFooterTask == TASK_NOT_BEGIN){
            mFooterHolder.begin(mFooter);
            return;
        }
        if (mCurrentFooterTask == TASK_READY){
            mFooterHolder.ready(mFooter);
            return;
        }
        if (mCurrentFooterTask == TASK_ING){
            mFooterHolder.ing(mFooter);
            return;
        }
        if (mCurrentFooterTask == TASK_END_SUCCESS){
            mFooterHolder.endSuccess(mFooter);
            return;
        }
        if (mCurrentFooterTask == TASK_END_FAILED){
            mFooterHolder.endFailed(mFooter);
            return;
        }

    }

    private void ChangeFooterStatus() {
        if (Bigger(Math.abs(mFooterMLP.topMargin),mFooterHolder.getFooterCompleteShowHeight())){
            mCurrentFooterStatus = STATUS_OVER_COMPLETE_SHOW;
            if (mCurrentFooterTask == TASK_NOT_BEGIN)
                mCurrentFooterTask = TASK_READY;
            return;
        }
        if (Equal(Math.abs(mFooterMLP.topMargin), mFooterHolder.getFooterCompleteShowHeight())){
            mCurrentFooterStatus = STATUS_COMPLETE_SHOW;
            if (mCurrentFooterTask == TASK_READY)
                mCurrentFooterTask = TASK_NOT_BEGIN;
            return;
        }
        if (Bigger(Math.abs(mFooterMLP.topMargin), 0)){
            mCurrentFooterStatus = STATUS_PART_SHOW;
            if (mCurrentFooterTask == TASK_READY)
                mCurrentFooterTask = TASK_NOT_BEGIN;
            return;
        }
        if (Equal(Math.abs(mFooterMLP.topMargin),0)) {
            mCurrentFooterStatus = STATUS_COMPLETE_HIDE;
            if (mCurrentFooterTask == TASK_READY)
                mCurrentFooterTask = TASK_NOT_BEGIN;
            return;
        }
    }
    private void ChangeHeaderUI() {
        if (mCurrentHeaderTask == TASK_NOT_BEGIN){
            mHeaderHolder.begin(mHeader);
            return;
        }
        if (mCurrentHeaderTask == TASK_READY){
            mHeaderHolder.ready(mHeader);
            return;
        }
        if (mCurrentHeaderTask == TASK_ING){
            mHeaderHolder.ing(mHeader);
            return;
        }
        if (mCurrentHeaderTask == TASK_END_SUCCESS){
            mHeaderHolder.endSuccess(mHeader);
            return;
        }
        if (mCurrentHeaderTask == TASK_END_FAILED){
            mHeaderHolder.endFailed(mHeader);
            return;
        }
    }

    private void ChangeHeaderStatus() {
        if (Bigger(Math.abs(mHeaderMLP.topMargin - mHeaderInitTopMargin),mHeaderHolder.getHeaderCompleteShowHeight())){
            mCurrentHeaderStatus = STATUS_OVER_COMPLETE_SHOW;
            if (mCurrentHeaderTask == TASK_NOT_BEGIN)
                mCurrentHeaderTask = TASK_READY;
            return;
        }
        if (Equal(mHeaderMLP.topMargin - mHeaderInitTopMargin, mHeaderHolder.getHeaderCompleteShowHeight())){
            mCurrentHeaderStatus = STATUS_COMPLETE_SHOW;
            if (mCurrentHeaderTask == TASK_READY)
                mCurrentHeaderTask = TASK_NOT_BEGIN;
            return;
        }
        if (Bigger(mHeaderMLP.topMargin - mHeaderInitTopMargin, 0)){
            mCurrentHeaderStatus = STATUS_PART_SHOW;
            if (mCurrentHeaderTask == TASK_READY)
                mCurrentHeaderTask = TASK_NOT_BEGIN;
            return;
        }
        if (Equal(mHeaderMLP.topMargin - mHeaderInitTopMargin,0)) {
            mCurrentHeaderStatus = STATUS_COMPLETE_HIDE;
            if (mCurrentHeaderTask == TASK_READY)
                mCurrentHeaderTask = TASK_NOT_BEGIN;
            return;
        }
    }


    private void MarginHeader(int dy) {
       mHeaderMLP.topMargin = mHeaderTopMarginWhenDown+dy/2;
       mHeader.setLayoutParams(mHeaderMLP);
    }
    private void MarginFooter(int dy) {
        mMidderMLP.topMargin = mFooterTopMarginWhenDown+dy/2;
        mMidderMLP.bottomMargin = -mMidderMLP.topMargin;
        mFooterMLP.topMargin = mMidderMLP.topMargin;
        mMidder.setLayoutParams(mMidderMLP);
        mFooter.setLayoutParams(mFooterMLP);
    }

    private boolean Bigger(int mayBigger,int maySmaller){
        if (Math.abs(mayBigger)-Math.abs(maySmaller) > 2)
            return true;
        return false;
    }
    private boolean Equal(int i1, int i2) {
        if (Math.abs(Math.abs(i1)-Math.abs(i2)) <= 2)
            return true;
        return false;
    }

    //    是否应该拦截mMidder的触摸事件
    private boolean shouldIntercept(){
        if (mCurrentHeaderStatus != STATUS_COMPLETE_HIDE ||
                mCurrentFooterStatus != STATUS_COMPLETE_HIDE)
            return true;
        return false;
    }

    private final static int WAIT_TIME = 1000;
    /**
     * 当任务成功完成时由使用者在外界调用
     */
    public void headerEndSuccess(){
        if (!mIsDown)
            mAllowTouch = false;
        mCurrentHeaderTask = TASK_END_SUCCESS;
        ChangeHeaderUI();
        wait(WAIT_TIME, new Runnable() {
            @Override
            public void run() {
                if (!mIsDown)
                    HideHeader();
                mCurrentHeaderTask = TASK_NOT_BEGIN;
                ChangeHeaderUI();
            }
        });
    }
    /**
     * 当任务失败完成时由使用者在外界调用
     */
    public void headerEndFailed(){
        if (!mIsDown)
            mAllowTouch = false;
        mCurrentHeaderTask = TASK_END_FAILED;
        ChangeHeaderUI();
        wait(WAIT_TIME, new Runnable() {
            @Override
            public void run() {
                if (!mIsDown)
                    HideHeader();
                mCurrentHeaderTask = TASK_NOT_BEGIN;
                ChangeHeaderUI();
            }
        });
    }

    public void footerEndSuccess(){
        if (!mIsDown)
            mAllowTouch = false;
        mCurrentFooterTask = TASK_END_SUCCESS;
        ChangeFooterUI();
        wait(WAIT_TIME, new Runnable() {
            @Override
            public void run() {
                if (!mIsDown)
                    HideFooter();
                mCurrentFooterTask = TASK_NOT_BEGIN;
                ChangeFooterUI();
            }
        });
    }

    public void footerEndFailed(){
        if (!mIsDown)
            mAllowTouch = false;
        mCurrentFooterTask = TASK_END_FAILED;
        ChangeFooterUI();
        wait(WAIT_TIME, new Runnable() {
            @Override
            public void run() {
                if (!mIsDown)
                    HideFooter();
                mCurrentFooterTask = TASK_NOT_BEGIN;
                ChangeFooterUI();
            }
        });
    }

    /**
     * 等待一段时间后开始执行方法
     * @param waitTime
     * @param runnable
     */
    private void wait(final long waitTime, final Runnable runnable){
        ValueAnimator valueAnimator = ValueAnimator.ofInt((int)waitTime,0);
        valueAnimator.setDuration(waitTime);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                runnable.run();
            }
        });
        valueAnimator.start();
    }

    /**
     * 使用上拉和下拉
     * @param callBack
     * @param header
     * @param midder
     * @param footer
     * @param headerHolder
     * @param footerHolder
     */
    public void UseAll(CallBack callBack,View header,View midder, View footer,
                       HeaderHolder headerHolder,FooterHolder footerHolder,Task task){
        this.removeAllViews();
        this.addView(header);
        this.addView(midder);
        this.addView(footer);
        this.callBack = callBack;
        this.mHeaderHolder = headerHolder;
        this.mFooterHolder = footerHolder;
        this.task = task;
        this.mHeader = header;
        this.mMidder = midder;
        this.mFooter = footer;
        attachTouch();
        loadOnce = false;
    }

    /**
     * 只使用下拉
     * @param callBack
     * @param header
     * @param midder
     * @param headerHolder
     */
    public void UseHeader(CallBack callBack,View header,View midder,HeaderHolder headerHolder,Task task){
        this.removeAllViews();
        this.addView(header);
        this.addView(midder);
        this.callBack = callBack;
        this.mHeaderHolder = headerHolder;
        this.task = task;
        this.mHeader = header;
        this.mMidder = midder;
        this.mFooter = null;
        attachTouch();
        loadOnce = false;
    }

    /**
     * 只使用上拉
     * @param callBack
     * @param footer
     * @param midder
     * @param footerHolder
     */
    public void UseFooter(CallBack callBack,View footer,View midder,FooterHolder footerHolder,Task task){
        this.removeAllViews();
        this.addView(midder);
        this.addView(footer);
        this.callBack = callBack;
        this.mFooterHolder = footerHolder;
        this.task = task;
        this.mHeader = null;
        this.mMidder = midder;
        this.mFooter = footer;
        attachTouch();
        loadOnce = false;
    }

    public interface UpdateUI{
        void begin(View v);
        void ready(View v);
        void ing(View v);
        void endSuccess(View v);
        void endFailed(View v);
    }
    public interface CallBack{
        //mMidder不能够继续下拉
        boolean CanotPullDown();
        //mMidder不能够继续上拉
        boolean CanotPullUp();
    }
    public interface Task{
        void HeaderTask();
        void FooterTask();
    }
}
