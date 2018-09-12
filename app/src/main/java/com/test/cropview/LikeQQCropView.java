package com.test.cropview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.test.cropview.tool.PhoneUtils;

public class LikeQQCropView extends View {


    private float centerX;
    private float centerY;

    public LikeQQCropView(Context context) {
        super(context);
        initGesture();
    }
    public LikeQQCropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initGesture();
    }
    public LikeQQCropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGesture();
    }


    //view显示的图片
    private Bitmap showBitmap;
    private RectF showBitmapRectF;
    //控制图片绘制的矩阵
    private Matrix showBitmapMatrix;
    private Paint showBitmapPaint;


    //初始化图片缩放和平移，保证图片在view中心显示
    private float initScale=1;
    private float initTranslateX;
    private float initTranslateY;


    //圆形所在矩阵
    private RectF circleRectF;
    //包裹圆形可触摸矩阵
    private RectF scaleRectF;

    //通过path在view中显示出圆形
    private Path circlePath;

    //圆形之外的path
    private Path outsidePath;

    private Paint paint;
    private Paint bgPaint;

    //是否可以移动图片(点击图片内部区域才能移动位置)
    private boolean canMoveBitmap;

    private GestureDetector gestureDetector;


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ContextCompat.getColor(getContext(),R.color.colorAccent));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        bgPaint=new Paint(Paint.ANTI_ALIAS_FLAG);

        bgPaint.setColor(ContextCompat.getColor(getContext(),R.color.transparent_half));

        showBitmapPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        showBitmapPaint.setColor(ContextCompat.getColor(getContext(),R.color.black));
        showBitmapPaint.setStyle(Paint.Style.STROKE);
        showBitmapPaint.setStrokeWidth(2);

        init();
    }
    public void setShowBitmap(Bitmap bitmap){
        showBitmap=bitmap;
    }
    public void init() {
        if(showBitmap==null){
            return;
        }

        circlePath=new Path();
        outsidePath=new Path();

        if(showBitmap.getHeight()<getHeight()&&showBitmap.getWidth()<getWidth()){
            initScale=1;
            initTranslateX=(getWidth()-showBitmap.getWidth())/2;
            initTranslateY=(getHeight()-showBitmap.getHeight())/2;


        }else{
            if(showBitmap.getWidth()*1.0f/showBitmap.getHeight()>getWidth()*1.0f/getHeight()){
                initScale=getWidth()*1.0f/showBitmap.getWidth();
                initTranslateX=0;
                initTranslateY=(getHeight()-showBitmap.getHeight()*initScale)/2;
            }else{
                initScale=getHeight()*1.0f/showBitmap.getHeight();
                initTranslateX=(getWidth()-showBitmap.getWidth()*initScale)/2;
                initTranslateY=0;
            }
        }

        //图片缩放之前的矩阵
        showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
        showBitmapMatrix =new Matrix();
        showBitmapMatrix.postScale(initScale,initScale);
        showBitmapMatrix.postTranslate(initTranslateX,initTranslateY);

        //图片缩放之前的矩阵
        showBitmapMatrix.mapRect(showBitmapRectF);

        float rectFW = showBitmapRectF.right - showBitmapRectF.left;
        float rectFH = showBitmapRectF.bottom - showBitmapRectF.top;

        //圆形所在矩阵边长
        float circleRectFLength=rectFW>rectFH?rectFH:rectFW;

        centerX = getWidth()/2;
        centerY = getHeight()/2;

        //计算出圆形所在矩阵的left top
        float circleRectFLeft= centerX -circleRectFLength/2;
        float circleRectFTop= centerY -circleRectFLength/2;
        //圆形矩阵
        circleRectF=new RectF(circleRectFLeft,circleRectFTop,circleRectFLength+circleRectFLeft,circleRectFLength+circleRectFTop);

//            circlePath.addRect(circleRectF, Path.Direction.CW);
        circlePath.addCircle(centerX, centerY,circleRectFLength/2, Path.Direction.CW);

        outsidePath.addRect(new RectF(0,0,getWidth(),getHeight()),Path.Direction.CW);

        outsidePath.op(circlePath, Path.Op.XOR);

        scaleRectF=new RectF();
        scaleRectF.set(circleRectF);
        scaleRectF.left=scaleRectF.left-getDP();
        scaleRectF.top=scaleRectF.top-getDP();
        scaleRectF.right=scaleRectF.right+getDP();
        scaleRectF.bottom=scaleRectF.bottom+getDP();

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(showBitmap==null){
           return;
        }
        canvas.drawBitmap(showBitmap, showBitmapMatrix,null);


        //包含圆形可点击区域矩形
        canvas.drawRect(circleRectF,paint);
        //包含图片矩形
        canvas.drawRect(showBitmapRectF,showBitmapPaint);//
        canvas.drawPath(outsidePath,bgPaint);

    }

    public int getDP(){
        int i = PhoneUtils.dip2px(getContext(), 10);
        return i;
    }

    private void initGesture() {
        gestureDetector=new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if(canMoveBitmap){

                    //从左往右滑动图片(防止图片滑出裁剪框外)
                    if(distanceX<0){
                        float rectDistance=circleRectF.left-showBitmapRectF.left;
                        if(rectDistance<Math.abs(distanceX)){
                            distanceX=-rectDistance;
                        }
                    }
                    //从右往左滑动图片(防止图片滑出裁剪框外)
                    if(distanceX>0){
                        float rectDistance=showBitmapRectF.right-circleRectF.right;
                        if(rectDistance<Math.abs(distanceX)){
                            distanceX=rectDistance;
                        }
                    }
                    //从上往下滑动图片(防止图片滑出裁剪框外)
                    if(distanceY<0){
                        float rectDistance=circleRectF.top-showBitmapRectF.top;
                        if(rectDistance<Math.abs(distanceY)){
                            distanceY=-rectDistance;
                        }
                    }

                    //从下往上滑动图片(防止图片滑出裁剪框外)
                    if(distanceY>0){
                        float rectDistance=showBitmapRectF.bottom-circleRectF.bottom;
                        if(rectDistance<Math.abs(distanceY)){
                            distanceY=rectDistance;
                        }
                    }

                    showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
                    showBitmapMatrix.postTranslate(-distanceX,-distanceY);
                    showBitmapMatrix.mapRect(showBitmapRectF);


                    invalidate();
                }
                return true;
            }
        });
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(showBitmapRectF.contains(event.getX(),event.getY())){
                    canMoveBitmap=true;
                    Log("===true");
                }else{
                    Log("===false");
                }
            break;
            case MotionEvent.ACTION_UP:
                canMoveBitmap=false;
            break;
        }
        return true;
    }

    private void Log(String s) {
        Log.i("@@@===","==="+s);
    }
}
