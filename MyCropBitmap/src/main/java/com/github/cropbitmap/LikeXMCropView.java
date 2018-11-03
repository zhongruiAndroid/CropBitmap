package com.github.cropbitmap;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/***
 *   created by zhongrui on 2018/10/24
 */
public class LikeXMCropView extends View {
    private void Log(String str) {
        Log.i("===", "@@===" + str);
    }

    ;
    private LikeXMCropViewUtils viewUtils;
    private GestureDetector gestureDetector;

    private int currentState = -1;
    private final int touch_status_bitmap = 0;
    private final int touch_status_left = 1;
    private final int touch_status_top = 2;
    private final int touch_status_right = 3;
    private final int touch_status_bottom = 4;
    private final int touch_status_left_top = 5;
    private final int touch_status_right_top = 6;
    private final int touch_status_left_bottom = 7;
    private final int touch_status_right_bottom = 8;

    private ValueAnimator animator;

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
        viewUtils = new LikeXMCropViewUtils();
        //设置触摸边距
        viewUtils.touchWidth = dip2px(15);
        //设置裁剪框上下，左右间隔距离
        viewUtils.borderDistance = viewUtils.touchWidth * 2 + dip2px(20);
        initGestureDetector();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(animator!=null&&animator.isRunning()){
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (viewUtils.leftBorderTouchRect.contains(event.getX(), event.getY())) {
                    /*左边框触摸*/
                    currentState = touch_status_left;
                    viewUtils.touchOffsetX=event.getX()-viewUtils.cropRect.left;
                } else if (viewUtils.topBorderTouchRect.contains(event.getX(), event.getY())) {
                    /*上边框触摸*/
                    currentState = touch_status_top;
                    viewUtils.touchOffsetY=event.getY()-viewUtils.cropRect.top;

                } else if (viewUtils.rightBorderTouchRect.contains(event.getX(), event.getY())) {
                    /*右边框触摸*/
                    currentState = touch_status_right;
                    viewUtils.touchOffsetX=viewUtils.cropRect.right-event.getX();

                } else if (viewUtils.bottomBorderTouchRect.contains(event.getX(), event.getY())) {
                    /*下边框触摸*/
                    currentState = touch_status_bottom;
                    viewUtils.touchOffsetY=viewUtils.cropRect.bottom-event.getY();

                } else if (viewUtils.leftTopTouchRect.contains(event.getX(), event.getY())) {
                    /*左上角触摸*/
                    currentState = touch_status_left_top;
                    viewUtils.touchOffsetX=event.getX()-viewUtils.cropRect.left;
                    viewUtils.touchOffsetY=event.getY()-viewUtils.cropRect.top;

                } else if (viewUtils.rightTopTouchRect.contains(event.getX(), event.getY())) {
                    /*右上角触摸*/
                    currentState = touch_status_right_top;
                    viewUtils.touchOffsetX=viewUtils.cropRect.right-event.getX();
                    viewUtils.touchOffsetY=event.getY()-viewUtils.cropRect.top;

                } else if (viewUtils.leftBottomTouchRect.contains(event.getX(), event.getY())) {
                    /*左下角触摸*/
                    currentState = touch_status_left_bottom;
                    viewUtils.touchOffsetX=event.getX()-viewUtils.cropRect.left;
                    viewUtils.touchOffsetY=viewUtils.cropRect.bottom-event.getY();

                } else if (viewUtils.rightBottomTouchRect.contains(event.getX(), event.getY())) {
                    /*右下角触摸*/
                    currentState = touch_status_right_bottom;
                    viewUtils.touchOffsetX=viewUtils.cropRect.right-event.getX();
                    viewUtils.touchOffsetY=viewUtils.cropRect.bottom-event.getY();

                } else if (viewUtils.showBitmapRect.contains(event.getX(), event.getY())) {
                    /*触摸图片*/
                    currentState = touch_status_bitmap;
                    Log("===触摸图片");
                }
                break;
            case MotionEvent.ACTION_UP:
                ////只要触摸view，就取消裁剪框自动放大和移动中心的动作，如果up之后无动作，才放大和平移

                /*只要移动了图片不管是否修正图片位置都需要重新计算图片所在Rect*/
                viewUtils.refreshShowBitmapRect();

                //如果是移动图片，则判断是否需要修正图片位置，保证图片在裁剪框内部
                if (currentState == touch_status_bitmap) {
                    updateBitmapLocation();
                }
                if(currentState != touch_status_bitmap&&currentState!=-1){
                    //移动裁剪框之后才修复裁剪框的位置
                    //裁剪框和图片移动至view中心和放大操作
                    updateCropBorderLocation();
                }
                //恢复边框和图片触摸状态
                currentState = -1;

                break;

        }
        gestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * 裁剪框和图片移动至view中心和放大操作
     */
    private void updateCropBorderLocation() {
        float rectWidth = viewUtils.cropRect.right - viewUtils.cropRect.left;
        float rectHeight = viewUtils.cropRect.bottom - viewUtils.cropRect.top;
        float offsetsX=0;
        float offsetsY=0;
        float scale=1f;
        if(rectHeight<getHeight()&&rectWidth<getWidth()){
            //需要进行放大和XY轴平移操作
            offsetsX=viewUtils.cropRect.left+getWidth()-viewUtils.cropRect.right;
            offsetsY=viewUtils.cropRect.top+getHeight()-viewUtils.cropRect.bottom;

            //计算放大倍数
            if(getWidth()*1f/rectWidth<getHeight()*1f/rectHeight){
                scale=getWidth()*1f/rectWidth;
            }else{
                scale=getHeight()*1f/rectHeight;
            }
        }else if(rectHeight<getHeight()){
            //需要Y轴平移
            offsetsY=viewUtils.cropRect.top+getHeight()-viewUtils.cropRect.bottom;
        }else if(rectWidth<getWidth()){
            //需要X轴平移
            offsetsX=viewUtils.cropRect.left+getWidth()-viewUtils.cropRect.right;
        }


        float translateX=0;
        float translateY=0;
        if(offsetsX!=0){
            translateX=(offsetsX*1f/2)-viewUtils.cropRect.left;
        }
        if(offsetsY!=0){
            translateY=(offsetsY*1f/2)-viewUtils.cropRect.top;
        }
        RectF beforeRectF=new RectF(viewUtils.cropRect);

        RectF afterRectF=new RectF(viewUtils.cropRect);
        Matrix matrix=new Matrix();
        matrix.postTranslate(translateX,translateY);
        matrix.postScale(scale,scale,getWidth()/2,getHeight()/2);
        matrix.mapRect(afterRectF);


        /*图片平移和放大*/
       /* viewUtils.showBitmapMatrix.postTranslate(translateX,translateY);
        viewUtils.showBitmapMatrix.postScale(scale,scale,getWidth()/2,getHeight()/2);
        viewUtils.refreshShowBitmapRect();*/

        /*viewUtils.cropRect.set(viewUtils.cropRect.left+translateX,
                viewUtils.cropRect.top+translateY,
                viewUtils.cropRect.right+translateX,
                viewUtils.cropRect.bottom+translateY);*/
        /*裁剪框平移和放大*/
       /* Matrix matrix=new Matrix();
        matrix.postTranslate(translateX,translateY);
        matrix.postScale(scale,scale,getWidth()/2,getHeight()/2);

        matrix.mapRect(viewUtils.cropRect);
        viewUtils.refreshPath();
        viewUtils.refreshTouchBorder(viewUtils.cropRect);

        invalidate();*/

        PropertyValuesHolder dst1X = PropertyValuesHolder.ofFloat("dst1X", viewUtils.cropRect.left, afterRectF.left);
        PropertyValuesHolder dst1Y = PropertyValuesHolder.ofFloat("dst1Y", viewUtils.cropRect.top, afterRectF.top);

        PropertyValuesHolder dst2X = PropertyValuesHolder.ofFloat("dst2X", viewUtils.cropRect.right, afterRectF.right);
        PropertyValuesHolder dst2Y = PropertyValuesHolder.ofFloat("dst2Y", viewUtils.cropRect.top, afterRectF.top);

        PropertyValuesHolder dst3X = PropertyValuesHolder.ofFloat("dst3X", viewUtils.cropRect.right, afterRectF.right);
        PropertyValuesHolder dst3Y = PropertyValuesHolder.ofFloat("dst3Y", viewUtils.cropRect.bottom, afterRectF.bottom);


        animator = ValueAnimator.ofPropertyValuesHolder(
                dst1X,
                dst1Y,
                dst2X,
                dst2Y,
                dst3X,
                dst3Y);
        final float[] dst = {beforeRectF.left,beforeRectF.top,beforeRectF.right,beforeRectF.top,beforeRectF.right,beforeRectF.bottom};
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float dst1X = (float) animation.getAnimatedValue("dst1X");
                float dst1Y = (float) animation.getAnimatedValue("dst1Y");

                float dst2X = (float) animation.getAnimatedValue("dst2X");
                float dst2Y = (float) animation.getAnimatedValue("dst2Y");

                float dst3X = (float) animation.getAnimatedValue("dst3X");
                float dst3Y = (float) animation.getAnimatedValue("dst3Y");

                Matrix tempMatrix=new Matrix();
                tempMatrix.setPolyToPoly(dst,0,new float[]{dst1X,dst1Y,dst2X,dst2Y,dst3X,dst3Y},0,3);

                viewUtils.showBitmapMatrix.postConcat(tempMatrix);

                viewUtils.cropRect.left=dst1X;
                viewUtils.cropRect.top=dst1Y;
                viewUtils.cropRect.right=dst2X;
                viewUtils.cropRect.bottom=dst3Y;

                viewUtils.refreshCropPath();
                viewUtils.refreshTouchBorder(viewUtils.cropRect);

                /*重新计算图片所在Rect*/
                viewUtils.refreshShowBitmapRect();

                dst[0]=dst1X;
                dst[1]=dst1Y;
                dst[2]=dst2X;
                dst[3]=dst2Y;
                dst[4]=dst3X;
                dst[5]=dst3Y;

                invalidate();
            }
        });
        animator.setDuration(410);
        animator.setInterpolator(null);
        animator.start();
    }

    /**
     * 修正图片位置，保证图片在裁剪框内部
     */
    private void updateBitmapLocation() {
        float tempX = 0;
        float tempY = 0;
        //图片需要向左移动
        if (viewUtils.showBitmapRect.left > viewUtils.cropRect.left) {
            tempX = viewUtils.cropRect.left - viewUtils.showBitmapRect.left;
        } else if (viewUtils.showBitmapRect.right < viewUtils.cropRect.right) {
            //图片需要向右移动
            tempX = viewUtils.cropRect.right - viewUtils.showBitmapRect.right;
        }
        if (viewUtils.showBitmapRect.top > viewUtils.cropRect.top) {
            //图片需要向上移动
            tempY = viewUtils.cropRect.top - viewUtils.showBitmapRect.top;
        } else if (viewUtils.showBitmapRect.bottom < viewUtils.cropRect.bottom) {
            //图片需要向下移动
            tempY = viewUtils.cropRect.bottom - viewUtils.showBitmapRect.bottom;
        }
        if (tempX != 0 || tempY != 0) {
            //为了让图片复位更有过度效果，这里加个动画
            float trans_x = getShowBitmapMatrixAttr(Matrix.MTRANS_X);
            float trans_y = getShowBitmapMatrixAttr(Matrix.MTRANS_Y);
            PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat("x", 0, tempX);
            PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat("y", 0, tempY);
            animator = ValueAnimator.ofPropertyValuesHolder(holderX, holderY);
            final float[] beforeX = {0};
            final float[] beforeY = {0};
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float x = (float) animation.getAnimatedValue("x");
                    float y = (float) animation.getAnimatedValue("y");
                    viewUtils.showBitmapMatrix.postTranslate(x - beforeX[0], y - beforeY[0]);
                    beforeX[0] = x;
                    beforeY[0] = y;
                    /*重新计算图片所在Rect*/
                    viewUtils.refreshShowBitmapRect();
                    invalidate();
                }
            });
            animator.setDuration(400);
            animator.start();

        }
    }

    /*获取matrix各个属性*/
    private float getShowBitmapMatrixAttr(int flag) {
        float[] values = new float[9];
        viewUtils.showBitmapMatrix.getValues(values);
        return values[flag];
    }
    private float getMatrixAttr(Matrix matrix,int flag) {
        float[] values = new float[9];
        matrix.getValues(values);
        return values[flag];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewUtils.centerX = w / 2;
        viewUtils.centerY = h / 2;

        viewUtils.viewWidth = w;
        viewUtils.viewHeight = h;


        viewUtils.initScale = 1f;
       /* if (viewUtils.showBitmap.getWidth() < w && viewUtils.showBitmap.getHeight() < h) {
            viewUtils.needMoveX = 1f * (w - viewUtils.showBitmap.getWidth()) / 2;
            viewUtils.needMoveY = 1f * (h - viewUtils.showBitmap.getHeight()) / 2;
        } else {*/
            if (viewUtils.showBitmap.getWidth() * 1f / viewUtils.showBitmap.getHeight() > w * 1f / h) {
                viewUtils.initScale = w * 1f / viewUtils.showBitmap.getWidth();
                viewUtils.needMoveX = 0;
                viewUtils.needMoveY = 1f * (h - viewUtils.showBitmap.getHeight() * viewUtils.initScale) / 2;

            } else {
                viewUtils.initScale = h * 1f / viewUtils.showBitmap.getHeight();
                viewUtils.needMoveX = 1f * (w - viewUtils.showBitmap.getWidth() * viewUtils.initScale) / 2;
                viewUtils.needMoveY = 0;
            }
//        }

        viewUtils.prepare();

        viewUtils.showBitmapRect.set(0, 0, viewUtils.showBitmap.getWidth(), viewUtils.showBitmap.getHeight());

        viewUtils.showBitmapMatrix.postScale(viewUtils.initScale, viewUtils.initScale);
        viewUtils.showBitmapMatrix.postTranslate(viewUtils.needMoveX, viewUtils.needMoveY);

        viewUtils.showBitmapMatrix.mapRect(viewUtils.showBitmapRect);



        Log(viewUtils.showBitmapRect.toString());///、、、

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        canvas.drawBitmap(viewUtils.showBitmap, viewUtils.showBitmapMatrix, null);

        canvas.drawPath(viewUtils.cropPath, viewUtils.cropPaint);
        canvas.drawPath(viewUtils.maskLayerPath, viewUtils.maskLayerPaint);

        ///
        canvas.drawRect(viewUtils.leftBorderTouchRect, viewUtils.cropPaint);
        canvas.drawRect(viewUtils.topBorderTouchRect, viewUtils.cropPaint);
        canvas.drawRect(viewUtils.rightBorderTouchRect, viewUtils.cropPaint);
        canvas.drawRect(viewUtils.bottomBorderTouchRect, viewUtils.cropPaint);

        canvas.drawRect(viewUtils.leftTopTouchRect, viewUtils.cropPaint);
        canvas.drawRect(viewUtils.rightTopTouchRect, viewUtils.cropPaint);
        canvas.drawRect(viewUtils.leftBottomTouchRect, viewUtils.cropPaint);
        canvas.drawRect(viewUtils.rightBottomTouchRect, viewUtils.cropPaint);
        ///
    }

    public void setBitMap(final Bitmap bitmap) {
        viewUtils.showBitmap = bitmap;
    }

    private void prepare() {
    }

    private int dip2px(float dipValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }

    private void initGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                    //X:左正右负,Y:上正下负
                    switch (currentState) {
                        case touch_status_left:
                            moveLeftBorder(distanceX, e2);
                            break;
                        case touch_status_top:
                            moveTopBorder(distanceY, e2);
                            break;
                        case touch_status_right:
                            moveRightBorder(distanceX, e2);
                            break;
                        case touch_status_bottom:
                            moveBottomBorder(distanceY, e2);
                            break;
                        case touch_status_left_top:
                            moveLeftBorder(distanceX, e2);
                            moveTopBorder(distanceY, e2);
                            break;
                        case touch_status_right_top:
                            moveTopBorder(distanceY, e2);
                            moveRightBorder(distanceX, e2);
                            break;
                        case touch_status_left_bottom:
                            moveLeftBorder(distanceX, e2);
                            moveBottomBorder(distanceY, e2);
                            break;
                        case touch_status_right_bottom:
                            moveRightBorder(distanceX, e2);
                            moveBottomBorder(distanceY, e2);
                            break;
                    }

                    viewUtils.refreshCropPath();
                    viewUtils.refreshTouchBorder(viewUtils.cropRect);

                if(touch_status_bitmap==currentState){
                    viewUtils.showBitmapMatrix.postTranslate(-distanceX, -distanceY);
                }
                invalidate();
//                return super.onScroll(e1, e2, distanceX, distanceY);
                return true;
            }
        });
    }

    private void moveBottomBorder(float distanceY, MotionEvent e2) {
        //下边框向下移动
        if (distanceY < 0) {

            if (viewUtils.cropRect.bottom <= viewUtils.showBitmapRect.bottom&&viewUtils.cropRect.bottom <=getHeight()) {
                //如果触摸点超出裁剪框范围移动无效，触摸点需要在裁剪框内部
                if(viewUtils.cropRect.bottom-viewUtils.touchOffsetY<=e2.getY()){
                    float tempDistance = Math.min(viewUtils.showBitmapRect.bottom - viewUtils.cropRect.bottom, Math.abs(distanceY));
                    tempDistance=Math.min(tempDistance,getHeight()-viewUtils.cropRect.bottom );
                    viewUtils.cropRect.bottom = viewUtils.cropRect.bottom + tempDistance;
                }
            } else {
                ////这个时候如果图片放大，超过下边部分，就需要缩小图片

            }
        } else {
            //下边框向上移动
            //如果触摸点超出裁剪框范围移动无效，触摸点需要在裁剪框内部
            if(viewUtils.cropRect.bottom-viewUtils.touchOffsetY>=e2.getY()){
                //需要比较下边和上边的距离,保持下上两边的间隔
                if (viewUtils.cropRect.top + viewUtils.borderDistance + Math.abs(distanceY) > viewUtils.cropRect.bottom) {
                    float tempDistance = viewUtils.cropRect.bottom - viewUtils.cropRect.top - viewUtils.borderDistance;

                    //计算裁剪框移动时，是否超过最小限制
                    viewUtils.cropRect.bottom =getCropBottom(tempDistance);
                } else {
                    viewUtils.cropRect.bottom = getCropBottom(Math.abs(distanceY));
                }
            }

        }
    }

    private void moveRightBorder(float distanceX, MotionEvent e2) {
        //右边框向右移动
        if (distanceX < 0) {

            if (viewUtils.cropRect.right <= viewUtils.showBitmapRect.right&&viewUtils.cropRect.right<=getWidth()) {
                //如果触摸点超出裁剪框范围移动无效，触摸点需要在裁剪框内部
                if(viewUtils.cropRect.right-viewUtils.touchOffsetX<=e2.getX()){
                    float tempDistance = Math.min(viewUtils.showBitmapRect.right - viewUtils.cropRect.right, Math.abs(distanceX));
                    //图片放大情况，右边部分超出屏幕，需要比较distance和right到右边屏幕的距离
                    tempDistance=Math.min(getWidth()-viewUtils.cropRect.right,tempDistance);
                    viewUtils.cropRect.right = viewUtils.cropRect.right + tempDistance;
                }
            } else {
                ////这个时候如果图片放大，超过右边部分，就需要缩小图片

            }
        } else {
            //右边框向左移动
            //如果触摸点超出裁剪框范围移动无效，触摸点需要在裁剪框内部
            if(viewUtils.cropRect.right-viewUtils.touchOffsetX>=e2.getX()){
                //需要比较左边和右边的距离,保持左右两边的间隔
                if (viewUtils.cropRect.left + viewUtils.borderDistance + Math.abs(distanceX) > viewUtils.cropRect.right) {
                    float tempDistance = viewUtils.cropRect.right - viewUtils.cropRect.left - viewUtils.borderDistance;

                    //计算裁剪框移动时，是否超过最小限制
                    viewUtils.cropRect.right = getCropRight(tempDistance);
                } else {
                    viewUtils.cropRect.right = getCropRight(Math.abs(distanceX));
                }
            }

        }
    }

    private void moveTopBorder(float distanceY, MotionEvent e2) {
        //上边框向上移动
        if (distanceY > 0) {
            //需要考虑图片上部分图片超出屏幕的情况
            if (viewUtils.cropRect.top >= viewUtils.showBitmapRect.top&&viewUtils.cropRect.top>=0) {
                //如果触摸点超出裁剪框范围移动无效，触摸点需要在裁剪框内部
                if(viewUtils.cropRect.top+viewUtils.touchOffsetY>=e2.getY()){
                    //比较裁剪框和图片的上边距离是否小于移动距离
                    float distance = Math.min(Math.abs(distanceY), Math.abs(viewUtils.cropRect.top - viewUtils.showBitmapRect.top));
                    distance=Math.min(distance,viewUtils.cropRect.top);
                    viewUtils.cropRect.top = viewUtils.cropRect.top - distance;
                }
            } else {
                ////这个时候如果图片放大，超过上边部分，就需要缩小图片

            }
        } else {
            //如果触摸点超出裁剪框范围移动无效，触摸点需要在裁剪框内部
            if (viewUtils.cropRect.top+viewUtils.touchOffsetY <= e2.getY()) {
                //上边框向下移动
                //需要保持上下两边的距离,保持上下的间隔
                if (viewUtils.cropRect.top + Math.abs(distanceY) + viewUtils.borderDistance > viewUtils.cropRect.bottom) {
                    float tempDistance = viewUtils.cropRect.bottom - viewUtils.borderDistance - viewUtils.cropRect.top;

                    //计算裁剪框移动时，是否超过最小限制
                    viewUtils.cropRect.top =getCropTop(tempDistance);
                } else {
                    viewUtils.cropRect.top = getCropTop(Math.abs(distanceY));
                }
            }
        }
    }

    private void moveLeftBorder(float distanceX, MotionEvent e2) {
        //左边框向左移动
        if (distanceX > 0) {
            if (viewUtils.cropRect.left >= viewUtils.showBitmapRect.left&&viewUtils.cropRect.left >=0) {
                //这个时候就不能向左移动裁剪框了
                //如果触摸点超出裁剪框范围移动无效，触摸点需要在裁剪框内部
                if(viewUtils.cropRect.left+viewUtils.touchOffsetX>=e2.getX()){
                    //比较裁剪框和图片的左边距离是否小于移动距离
                    float distance = Math.min(Math.abs(distanceX), Math.abs(viewUtils.cropRect.left - viewUtils.showBitmapRect.left));
                    distance=Math.min(distance,viewUtils.cropRect.left);
                    viewUtils.cropRect.left = viewUtils.cropRect.left - distance;
                }
            } else {
                ////这个时候如果图片放大，超过左边部分，就需要缩小图片
            }
        } else {
            //左边框向右移动
            //如果触摸点超出裁剪框范围移动无效，触摸点需要在裁剪框内部
            if(viewUtils.cropRect.left+viewUtils.touchOffsetX<=e2.getX()){
                //需要比较左边和右边的距离,保持左右两边的间隔
                if (viewUtils.cropRect.left + Math.abs(distanceX) + viewUtils.borderDistance > viewUtils.cropRect.right) {
                    float tempDistance = viewUtils.cropRect.right - viewUtils.borderDistance - viewUtils.cropRect.left;

                    //计算裁剪框移动时，是否超过最小限制
                    viewUtils.cropRect.left=getCropLeft(tempDistance);
                } else {
                    viewUtils.cropRect.left = getCropLeft(Math.abs(distanceX));
                }
            }

        }
    }

    /*
    * 计算裁剪框移动时，是否超过最小限制
    * */
    private float getCropLeft(float tempDistance) {
        //计算是否超过裁剪框最小值
        float tempLeft = viewUtils.cropRect.left + tempDistance;
        if(viewUtils.cropRect.right - tempLeft>=viewUtils.getMinCropWidth()){
            return  tempLeft;
        }else{
            return viewUtils.cropRect.left = viewUtils.cropRect.right - viewUtils.getMinCropWidth();
        }
    }

    /*
     * 计算裁剪框移动时，是否超过最小限制
     * */
    private float getCropTop(float tempDistance) {
        //计算是否超过裁剪框最小值
        float tempTop = viewUtils.cropRect.top + tempDistance;

        if(viewUtils.cropRect.bottom-tempTop>=viewUtils.getMinCropWidth()){
            return  tempTop;
        }else{
            return   viewUtils.cropRect.bottom -viewUtils.getMinCropWidth();
        }
    }

    /*
     * 计算裁剪框移动时，是否超过最小限制
     * */
    private float getCropRight(float tempDistance) {
        //计算是否超过裁剪框最小值
        float tempRight = viewUtils.cropRect.right - tempDistance;

        if(tempRight-viewUtils.cropRect.left>=viewUtils.getMinCropWidth()){
            return  tempRight;
        }else{
            return  viewUtils.cropRect.left +viewUtils.getMinCropWidth();
        }
    }

    /*
     * 计算裁剪框移动时，是否超过最小限制
     * */
    private float getCropBottom(float tempDistance) {
        //计算是否超过裁剪框最小值
        float tempBottom = viewUtils.cropRect.bottom - tempDistance;

        if(tempBottom-viewUtils.cropRect.top>=viewUtils.getMinCropWidth()){
            return  tempBottom;
        }else{
            return  viewUtils.cropRect.top +viewUtils.getMinCropWidth();
        }
    }
}
