package com.test.cropview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.test.cropview.tool.PhoneUtils;
/***
 *   created by zhongrui on 2018/9/11
 */
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
    //图片可放大的最大倍数
    private float maxScale=3f;
    //双击图片放大倍数
    protected float doubleClickScale=1.8f;
    protected float doubleClickX;
    protected float doubleClickY;


    //随着圆形区域的变小而变小,用来限制圆形的缩小倍数
    private float minCircleScale=1f;
    //初始化图片缩放和平移，保证图片在view中心显示
    private float initScale=1f;
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


    private Region touchRegion;
    //1:左上角，2右上角，3右下角，4左下角
    private int touchArea;
    private int touchLength=10;

    //是否可以移动图片(点击图片内部区域才能移动位置)
    private boolean canMoveBitmap;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private ValueAnimator valueAnimator;

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

    public float getCurrentScale(){
        float[]temp=new float[9];
        showBitmapMatrix.getValues(temp);
        return temp[Matrix.MSCALE_X];
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

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                switch (e.getAction()){
                    case MotionEvent.ACTION_UP:
                        if(showBitmapRectF.contains(e.getX(),e.getY())){
                            if(getCurrentScale()>initScale){
                                //用于双击图片放大缩小,获取动画间隔缩放系数
                                final SparseArray<Float> sparseArray=new SparseArray<>();
                                sparseArray.put(0,-1f);
                                sparseArray.put(1,-1f);
                                valueAnimator=ValueAnimator.ofFloat(getCurrentScale(),initScale);
                                Log(initScale+"==="+initScale);
                                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        float value = (float) animation.getAnimatedValue();
                                        float tempScale=1;
                                        if(sparseArray.get(0)==-1&&sparseArray.get(1)==-1){
                                            sparseArray.put(0,value);
                                        }else if(sparseArray.get(1)==-1){
                                            sparseArray.put(1,value);
                                            tempScale=sparseArray.get(1)/sparseArray.get(0);
                                        }else{
                                            sparseArray.put(0,sparseArray.get(1));
                                            sparseArray.put(1,value);
                                            tempScale=sparseArray.get(1)/sparseArray.get(0);
                                        }
                                        zoomBitmap(tempScale,centerX,centerX);
                                        invalidate();
                                    }
                                });
                                valueAnimator.setInterpolator(new DecelerateInterpolator());
                                valueAnimator.setDuration(300);
                                valueAnimator.start();
                            }else{
                                doubleClickX=e.getX();
                                doubleClickY=e.getY();
                                //用于双击图片放大缩小,获取动画间隔缩放系数
                                final SparseArray<Float> sparseArray=new SparseArray<>();
                                sparseArray.put(0,-1f);
                                sparseArray.put(1,-1f);
                                valueAnimator=ValueAnimator.ofFloat(getCurrentScale(),doubleClickScale);
                                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        float value = (float) animation.getAnimatedValue();
                                        float tempScale=1;
                                        if(sparseArray.get(0)==-1&&sparseArray.get(1)==-1){
                                            sparseArray.put(0,value);
                                        }else if(sparseArray.get(1)==-1){
                                            sparseArray.put(1,value);
                                            tempScale=sparseArray.get(1)/sparseArray.get(0);
                                        }else{
                                            sparseArray.put(0,sparseArray.get(1));
                                            sparseArray.put(1,value);
                                            tempScale=sparseArray.get(1)/sparseArray.get(0);
                                        }
                                        showBitmapMatrix.postScale(tempScale,tempScale,doubleClickX,doubleClickY);
                                        showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
                                        showBitmapMatrix.mapRect(showBitmapRectF);
                                        invalidate();

                                    }
                                });
                                valueAnimator.setInterpolator(new DecelerateInterpolator());
                                valueAnimator.setDuration(300);
                                valueAnimator.start();
                            }
                        }

                        break;
                }
                return super.onDoubleTapEvent(e);
            }
        });
        scaleGestureDetector=new ScaleGestureDetector(getContext(),new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float currentScale = getCurrentScale();
                float scaleFactor = detector.getScaleFactor();

                //防止过度缩小
                if(currentScale*scaleFactor<initScale){
                    scaleFactor=initScale/currentScale;
                }

                /*showBitmapMatrix.postScale(scaleFactor,scaleFactor,detector.getFocusX(),detector.getFocusY());

                showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
                showBitmapMatrix.mapRect(showBitmapRectF);


                //如果缩小需要检查圆形框是否包含图片，如果不包含，缩小之后需要平移
                if(scaleFactor<1){
                    float leftLength = showBitmapRectF.left - circleRectF.left;
                    if(leftLength>0){
                        showBitmapMatrix.postTranslate(-leftLength,0);
                    }
                    float topLength = showBitmapRectF.top - circleRectF.top;
                    if(topLength>0){
                        showBitmapMatrix.postTranslate(0,-topLength);
                    }
                    float rightLength = circleRectF.right-showBitmapRectF.right ;
                    if(rightLength>0){
                        showBitmapMatrix.postTranslate(rightLength,0);
                    }
                    float bottomLength = circleRectF.bottom-showBitmapRectF.bottom ;
                    if(bottomLength>0){
                        showBitmapMatrix.postTranslate(0,bottomLength);
                    }
                    showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
                    showBitmapMatrix.mapRect(showBitmapRectF);
                }*/
                zoomBitmap(scaleFactor,detector.getFocusX(),detector.getFocusY());
                invalidate();
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                //如果缩放中心在图片范围内就可以缩放
                if(showBitmapRectF.contains(detector.getFocusX(),detector.getFocusY())){
                    return true;
                }else{
                    return false;
                }
            }
        });
    }
    private void zoomBitmap(float scaleFactor, float focusX, float focusY){
        if(scaleFactor>1&&getCurrentScale()*scaleFactor>maxScale){
            scaleFactor=maxScale/getCurrentScale();
        }
        showBitmapMatrix.postScale(scaleFactor,scaleFactor,focusX,focusY);

        showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
        showBitmapMatrix.mapRect(showBitmapRectF);


        //如果缩小需要检查圆形框是否包含图片，如果不包含，缩小之后需要平移
        if(scaleFactor<1){//小于1缩小动作，大于1放大动作
            float leftLength = showBitmapRectF.left - circleRectF.left;
            if(leftLength>0){
                showBitmapMatrix.postTranslate(-leftLength,0);
            }
            float topLength = showBitmapRectF.top - circleRectF.top;
            if(topLength>0){
                showBitmapMatrix.postTranslate(0,-topLength);
            }
            float rightLength = circleRectF.right-showBitmapRectF.right ;
            if(rightLength>0){
                showBitmapMatrix.postTranslate(rightLength,0);
            }
            float bottomLength = circleRectF.bottom-showBitmapRectF.bottom ;
            if(bottomLength>0){
                showBitmapMatrix.postTranslate(0,bottomLength);
            }
            showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
            showBitmapMatrix.mapRect(showBitmapRectF);
        }

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(showBitmapRectF.contains(event.getX(),event.getY())){
                    canMoveBitmap=true;
                }else{
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
