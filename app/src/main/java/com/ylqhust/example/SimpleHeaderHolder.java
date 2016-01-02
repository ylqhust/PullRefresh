package com.ylqhust.example;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ylqhust.pullrefresh.HeaderHolder;

/**
 * Created by apple on 16/1/1.
 */
public class SimpleHeaderHolder extends HeaderHolder {
    private View v;
    private ImageView arrow;
    private ImageView result;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private Bitmap arrowDown;
    private Bitmap arrowUp;
    private Bitmap success;
    private Bitmap failed;
    private Context cxt;
    public SimpleHeaderHolder(Context context){
        this.cxt = context;
        arrowDown = ResBitmap(R.drawable.pulldown_icon_big);
        arrowUp = ResBitmap(R.drawable.pullup_icon_big);
        success = ResBitmap(R.drawable.pullrefresh_success);
        failed = ResBitmap(R.drawable.ic_error_white_18dp);
    }

    private Bitmap ResBitmap(int id){
        return BitmapFactory.decodeResource(cxt.getResources(),id);
    }

    private void initView(View v) {
        this.v = v;
        arrow = (ImageView) v.findViewById(R.id.pullrefresh_header_img_status);
        progressBar = (ProgressBar) v.findViewById(R.id.pullrefresh_header_progressbar);
        tvStatus = (TextView) v.findViewById(R.id.pullrefresh_header_tv_status);
        result = (ImageView) v.findViewById(R.id.pullrefresh_header_img_result);
    }

    @Override
    public void begin(View v) {
        if (this.v==null)
            initView(v);
        result.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        arrow.setVisibility(View.VISIBLE);
        arrow.setImageBitmap(arrowDown);
        tvStatus.setText("下拉刷新");
    }

    @Override
    public void ready(View v) {
        if (this.v==null)
            initView(v);
        result.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        arrow.setVisibility(View.VISIBLE);
        arrow.setImageBitmap(arrowUp);
        tvStatus.setText("释放立即刷新");
    }

    @Override
    public void ing(View v) {
        if (this.v==null)
            initView(v);
        result.setVisibility(View.GONE);
        arrow.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("正在刷新...");
    }

    @Override
    public void endSuccess(View v) {
        if (this.v==null)
            initView(v);
        result.setImageBitmap(success);
        result.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        arrow.setVisibility(View.GONE);
        tvStatus.setText("刷新成功");
    }

    @Override
    public void endFailed(View v) {
        if (this.v==null)
            initView(v);
        result.setImageBitmap(failed);

        result.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        arrow.setVisibility(View.GONE);
        tvStatus.setText("刷新失败");
    }

    @Override
    public int getHeaderCompleteShowHeight() {
        return DimenUtils.Dp2Px(60);
    }
}
