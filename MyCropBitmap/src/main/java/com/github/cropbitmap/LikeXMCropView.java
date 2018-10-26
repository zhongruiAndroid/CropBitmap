package com.github.cropbitmap;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/***
 *   created by zhongrui on 2018/10/24
 */
public class LikeXMCropView extends View {
    private LikeXMCropViewUtils viewUtils;
    public LikeXMCropView(Context context) {
        super(context);
        init(null);
    }

    public LikeXMCropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LikeXMCropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewUtils=new LikeXMCropViewUtils();
        viewUtils.prepare();
    }
}
