package com.ylqhust.pullrefresh;


/**
 * Created by ylqhust on 16/1/1.
 */
public abstract class FooterHolder implements PullRefresh.UpdateUI {
    //获取尾部完全显示的高度，这个高度用来判断是否改变尾部状态，单位是px
    public abstract int getFooterCompleteShowHeight();
}
