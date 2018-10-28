package com.github.cropbitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
    private void Log(String str){
        Log.i("===","@@==="+str);
    };
    private LikeXMCropViewUtils viewUtils;
    private GestureDetector gestureDetector;

    private int currentState=-1;
    private final int touch_status_left=1;
    private final int touch_status_top=2;
    private final int touch_status_right=3;
    private final int touch_status_bottom=4;
    private final int touch_status_left_top=5;
    private final int touch_status_right_top=6;
    private final int touch_status_left_bottom=7;
    private final int touch_status_right_bottom=8;

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
        viewUtils=new LikeXMCropViewUtils();
        //设置触摸边距
        viewUtils.touchWidth=dip2px(15);
        //设置裁剪框上下，左右间隔距离
        viewUtils.borderDistance=viewUtils.touchWidth*2+dip2px(20);

        gestureDetector=new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //X:左正右负,Y:上正下负
                switch (currentState){
                    case touch_status_left:
                        //左边框向左移动
                        if(distanceX>0){
                            ////这个时候如果图片放大，超过左边部分，就需要缩小图片
                            if(viewUtils.cropRect.left<=viewUtils.showBitmapRect.left){
                                //这个时候就不能向左移动裁剪框了
                            }else{
                                //比较裁剪框和图片的左边距离是否小于移动距离
                                float distance = Math.min(Math.abs(distanceX), Math.abs(viewUtils.cropRect.left - viewUtils.showBitmapRect.left));
                                viewUtils.cropRect.left=viewUtils.cropRect.left-distance;
                            }
                        }else{
                            //左边框向右移动
                            //需要比较左边和右边的距离,保持左右两边的间隔
                            if(viewUtils.cropRect.left+Math.abs(distanceX)+viewUtils.borderDistance>viewUtils.cropRect.right){
                                float tempDistance = viewUtils.cropRect.right - viewUtils.borderDistance-viewUtils.cropRect.left;
                                viewUtils.cropRect.left=viewUtils.cropRect.left+tempDistance;
                            }else{
                                viewUtils.cropRect.left=viewUtils.cropRect.left+Math.abs(distanceX);
                            }
                        }
                    break;
                    case touch_status_top:
                        //上边框向上移动
                        if(distanceY>0){
                            ////这个时候如果图片放大，超过上边部分，就需要缩小图片
                            if(viewUtils.cropRect.top<=viewUtils.showBitmapRect.top){

                            }else{
                                //比较裁剪框和图片的上边距离是否小于移动距离
                                float distance = Math.min(Math.abs(distanceY), Math.abs(viewUtils.cropRect.top - viewUtils.showBitmapRect.top));
                                viewUtils.cropRect.top=viewUtils.cropRect.top-distance;
                            }
                        }else{
                            //上边框向下移动
                            //需要保持上下两边的距离,保持上下的间隔
                            if(viewUtils.cropRect.top+Math.abs(distanceY)+viewUtils.borderDistance>viewUtils.cropRect.bottom){
                                float tempDistance =viewUtils.cropRect.bottom-viewUtils.borderDistance-viewUtils.cropRect.top;
                                viewUtils.cropRect.top=viewUtils.cropRect.top+tempDistance;
                            }else{
                                viewUtils.cropRect.top=viewUtils.cropRect.top+Math.abs(distanceY);
                            }
                        }
                    break;
                    case touch_status_right:
                        //右边框向右移动
                        if(distanceX<0){
                            ////这个时候如果图片放大，超过右边部分，就需要缩小图片
                            if(viewUtils.cropRect.right>=viewUtils.showBitmapRect.right){

                            }else{
                                float tempDistance = Math.min(viewUtils.showBitmapRect.right - viewUtils.cropRect.right, Math.abs(distanceX));
                                viewUtils.cropRect.right=viewUtils.cropRect.right+tempDistance;
                            }
                        }else{
                            //右边框向左移动
                            //需要比较左边和右边的距离,保持左右两边的间隔
                            if(viewUtils.cropRect.left+viewUtils.borderDistance+Math.abs(distanceX)>viewUtils.cropRect.right){
                                float tempDistance = viewUtils.cropRect.right - viewUtils.cropRect.left - viewUtils.borderDistance;
                                viewUtils.cropRect.right=viewUtils.cropRect.right-tempDistance;
                            }else{
                                viewUtils.cropRect.right=viewUtils.cropRect.right-Math.abs(distanceX);
                            }
                        }
                    break;
                    case touch_status_bottom:
                        //下边框向下移动
                        if(distanceY<0){
                            ////这个时候如果图片放大，超过下边部分，就需要缩小图片
                            if(viewUtils.cropRect.bottom>=viewUtils.showBitmapRect.bottom){

                            }else{
                                float tempDistance = Math.min(viewUtils.showBitmapRect.bottom - viewUtils.cropRect.bottom, Math.abs(distanceY));
                                viewUtils.cropRect.bottom=viewUtils.cropRect.bottom+tempDistance;
                            }
                        }else{
                            //下边框向上移动
                            //需要比较下边和上边的距离,保持下上两边的间隔
                            if(viewUtils.cropRect.top+viewUtils.borderDistance+Math.abs(distanceY)>viewUtils.cropRect.bottom){
                                float tempDistance = viewUtils.cropRect.bottom - viewUtils.cropRect.top - viewUtils.borderDistance;
                                viewUtils.cropRect.bottom=viewUtils.cropRect.bottom-tempDistance;
                            }else{
                                viewUtils.cropRect.bottom=viewUtils.cropRect.bottom-Math.abs(distanceY);
                            }
                        }
                    break;
                    case touch_status_left_top:

                    break;
                    case touch_status_right_top:

                    break;
                    case touch_status_left_bottom:

                    break;
                    case touch_status_right_bottom:

                    break;
                }
                viewUtils.refreshPath();
                viewUtils.refreshTouchBorder(viewUtils.cropRect);
                invalidate();
//                return super.onScroll(e1, e2, distanceX, distanceY);
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log("ACTION_DOWN");
                if(viewUtils.leftBorderTouchRect.contains(event.getX(),event.getY())){
                    /*左边框触摸*/
                    currentState=touch_status_left;
                }else if(viewUtils.topBorderTouchRect.contains(event.getX(),event.getY())){
                    /*上边框触摸*/
                    currentState=touch_status_top;

                }else if(viewUtils.rightBorderTouchRect.contains(event.getX(),event.getY())){
                    /*右边框触摸*/
                    currentState=touch_status_right;

                }else if(viewUtils.bottomBorderTouchRect.contains(event.getX(),event.getY())){
                    /*下边框触摸*/
                    currentState=touch_status_bottom;

                }else if(viewUtils.leftTopTouchRect.contains(event.getX(),event.getY())){
                    /*左上角触摸*/
                    currentState=touch_status_left_top;

                }else if(viewUtils.rightTopTouchRect.contains(event.getX(),event.getY())){
                    /*右上角触摸*/
                    currentState=touch_status_right_top;

                }else if(viewUtils.leftBottomTouchRect.contains(event.getX(),event.getY())){
                    /*左下角触摸*/
                    currentState=touch_status_left_bottom;

                }else if(viewUtils.rightBottomTouchRect.contains(event.getX(),event.getY())){
                    /*右下角触摸*/
                    currentState=touch_status_right_bottom;

                }
            break;
            case MotionEvent.ACTION_UP:
                currentState=-1;
                Log("ACTION_UP");
//                invalidate();
            break;
        }
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewUtils.centerX=w/2;
        viewUtils.centerY=h/2;

        viewUtils.viewWidth=w;
        viewUtils.viewHeight=h;


        viewUtils.initScale=1;
        if(viewUtils.showBitmap.getWidth()<w&&viewUtils.showBitmap.getHeight()<h){
            viewUtils.needMoveX=1f*(w-viewUtils.showBitmap.getWidth())/2;
            viewUtils.needMoveY=1f*(h-viewUtils.showBitmap.getHeight())/2;
        }else{
            if(viewUtils.showBitmap.getWidth()*1f/viewUtils.showBitmap.getHeight()>w*1f/h){
                viewUtils.initScale=w*1f/viewUtils.showBitmap.getWidth();
                viewUtils.needMoveX=0;
                viewUtils.needMoveY=1f*(h-viewUtils.showBitmap.getHeight()*viewUtils.initScale)/2;
            }else{
                viewUtils.initScale=h*1f/viewUtils.showBitmap.getHeight();
                viewUtils.needMoveX=1f*(w-viewUtils.showBitmap.getWidth()*viewUtils.initScale)/2;
                viewUtils.needMoveY=0;
            }
        }

        viewUtils.prepare();

        viewUtils.showBitmapRect.set(0,0,viewUtils.showBitmap.getWidth(),viewUtils.showBitmap.getHeight());

        viewUtils.showBitmapMatrix.postScale(viewUtils.initScale,viewUtils.initScale);
        viewUtils.showBitmapMatrix.postTranslate(viewUtils.needMoveX,viewUtils.needMoveY);

        viewUtils.showBitmapMatrix.mapRect(viewUtils.showBitmapRect);

//        viewUtils.showBitmapMatrix.setRectToRect();

        Log(viewUtils.showBitmapRect.toString());///、、、
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        canvas.drawBitmap(viewUtils.showBitmap,viewUtils.showBitmapMatrix,null);

        canvas.drawPath(viewUtils.cropPath,viewUtils.cropPaint);
        canvas.drawPath(viewUtils.maskLayerPath,viewUtils.maskLayerPaint);

        ///
        canvas.drawRect(viewUtils.leftBorderTouchRect,viewUtils.cropPaint);
        canvas.drawRect(viewUtils.topBorderTouchRect,viewUtils.cropPaint);
        canvas.drawRect(viewUtils.rightBorderTouchRect,viewUtils.cropPaint);
        canvas.drawRect(viewUtils.bottomBorderTouchRect,viewUtils.cropPaint);
//
        canvas.drawRect(viewUtils.leftTopTouchRect,viewUtils.cropPaint);
        canvas.drawRect(viewUtils.rightTopTouchRect,viewUtils.cropPaint);
        canvas.drawRect(viewUtils.leftBottomTouchRect,viewUtils.cropPaint);
        canvas.drawRect(viewUtils.rightBottomTouchRect,viewUtils.cropPaint);
        ///
    }
    public void setBitMap(final Bitmap bitmap){
        viewUtils.showBitmap=bitmap;
    }
    private void prepare() {
    }
    private int dip2px(float dipValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5F);
    }
}
