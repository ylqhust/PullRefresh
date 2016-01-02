package com.ylqhust.pullrefresh;

import com.ylqhust.pullrefresh.PullRefresh;

/**
 * Created by ylqhust on 16/1/1.
 */
public abstract class HeaderHolder implements PullRefresh.UpdateUI{
    //获取头部完全显示的高度，这个高度用来判断是否改变头部状态，单位是px
    public abstract int getHeaderCompleteShowHeight();
}
