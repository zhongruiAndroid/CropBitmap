package com.github.cropbitmap;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import java.io.FileDescriptor;
import java.io.InputStream;

/***
 *   created by zhongrui on 2018/9/11
 */
public class LikeQQCropView extends View {


    private float centerX;
    private float centerY;

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
    //只是用来记录初始位置
    private RectF initCircleRectF;
    //控制圆形所在矩阵
    private Matrix circleRectFMatrix;

    //包裹圆形可触摸矩阵
    private RectF bigCircleRectF;

    //通过path在view中显示出圆形
    private Path circlePath;
    //给圆形path内部绘制一个边框(like qq)
    private Path circleBorderPath;
    private Paint circleBorderPaint;

    //圆形之外所有区域
    private Path outsidePath;

    private Paint paint;
    private Paint bgPaint;



    private Region touchRegion;//(暂时没用)
    //1:左上角，2右上角，3右下角，4左下角(暂时没用)
    private int touchArea;
    //圆形外部可触摸宽度(暂时没用)
    private int touchLength=10;
    //用于放大圆形(暂时没用)
    private Path bigCirclePath;

    //是否可以放大圆形(暂时没用)
    private boolean canZoomCircle;

    //是否可以移动图片(点击图片内部区域才能移动位置)
    private boolean canMoveBitmap;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private ValueAnimator valueAnimator;


    private float radius=-1;
    private int maskColor;
    private int borderColor;


    public float getRadius() {
        return radius;
    }
    public float getClipWidth() {
        return getRectLength(circleRectF);
    }

    public LikeQQCropView setRadius(float radius) {
        this.radius = radius;
        post(new Runnable() {
            @Override
            public void run() {
                refreshPath();
                invalidate();
            }
        });

        return this;
    }

    public int getMaskColor() {
        return maskColor;
    }

    public LikeQQCropView setMaskColor(@ColorInt int maskColor) {
        this.maskColor = maskColor;
        post(new Runnable() {
            @Override
            public void run() {
                refreshPaint();
                invalidate();
            }
        });
        return this;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public LikeQQCropView setBorderColor(@ColorInt int borderColor) {
        this.borderColor = borderColor;
        post(new Runnable() {
            @Override
            public void run() {
                refreshPaint();
                invalidate();
            }
        });
        return this;
    }

    public float getMaxScale() {
        return maxScale;
    }

    public LikeQQCropView setMaxScale(float maxScale) {
        if(maxScale<1){
            maxScale=1;
        }
        if(doubleClickScale>maxScale){
            doubleClickScale=maxScale;
        }
        this.maxScale = maxScale;
        return this;
    }

    public float getDoubleClickScale() {
        return doubleClickScale;
    }

    public LikeQQCropView setDoubleClickScale(float doubleClickScale) {
        if(doubleClickScale<1){
            doubleClickScale=1;
        }
        if(doubleClickScale>maxScale){
            doubleClickScale=maxScale;
        }
        this.doubleClickScale = doubleClickScale;
        return this;
    }

    private boolean sizeChanged;
    public LikeQQCropView(Context context) {
        super(context);
        initGesture();
        initAttr(null);
    }


    public LikeQQCropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initGesture();
        initAttr(attrs);
    }
    public LikeQQCropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGesture();
        initAttr(attrs);
    }

    private void initAttr(AttributeSet attrs) {
        maskColor= Color.parseColor("#60000000");
        borderColor=ContextCompat.getColor(getContext(),android.R.color.white);
        if(attrs==null){
            return;
        }

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs,R.styleable.LikeQQCropView);
        maskColor = typedArray.getColor(R.styleable.LikeQQCropView_maskColor, Color.parseColor("#60000000"));
        borderColor = typedArray.getColor(R.styleable.LikeQQCropView_borderColor,ContextCompat.getColor(getContext(),android.R.color.white));
        radius = typedArray.getDimension(R.styleable.LikeQQCropView_radius, -1);

        maxScale = typedArray.getFloat(R.styleable.LikeQQCropView_maxScale, 3f);
        doubleClickScale = typedArray.getFloat(R.styleable.LikeQQCropView_doubleClickScale, 1.8f);

        if(maxScale<1){
            maxScale=1;
        }
        if(doubleClickScale<1){
            doubleClickScale=1;
        }
        if(doubleClickScale>maxScale){
            doubleClickScale=maxScale;
        }
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width=getScreenWidth()/2;
        int height=getScreenWidth()/2;

        if(ViewGroup.LayoutParams.WRAP_CONTENT==getLayoutParams().width&&ViewGroup.LayoutParams.WRAP_CONTENT==getLayoutParams().height){
            setMeasuredDimension(width,height);
        }else if(ViewGroup.LayoutParams.WRAP_CONTENT==getLayoutParams().width){
            setMeasuredDimension(width,heightSize);
        }else if(ViewGroup.LayoutParams.WRAP_CONTENT==getLayoutParams().height){
            setMeasuredDimension(widthSize,height);
        }else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }
    private int getScreenWidth() {
        return getContext().getResources().getDisplayMetrics().widthPixels;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        touchRegion=new Region();
        paint=new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setColor(ContextCompat.getColor(getContext(),R.color.colorAccent));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        circleBorderPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        circleBorderPaint.setColor(borderColor);
        circleBorderPaint.setStyle(Paint.Style.STROKE);
        circleBorderPaint.setStrokeWidth(dip2px(getContext(),1));

        bgPaint=new Paint(Paint.ANTI_ALIAS_FLAG);

        bgPaint.setColor(maskColor);
//        bgPaint.setStyle(Paint.Style.FILL);

        showBitmapPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
//        showBitmapPaint.setColor(ContextCompat.getColor(getContext(),R.color.black));
        showBitmapPaint.setStyle(Paint.Style.STROKE);
        showBitmapPaint.setStrokeWidth(2);
        init();
        sizeChanged=true;
    }

    private void init() {

        centerX = getWidth()/2;
        centerY = getHeight()/2;
        circleBorderPath=new Path();
        circlePath=new Path();
        outsidePath=new Path();
        bigCirclePath =new Path();
        if(showBitmap==null){
            return;
        }
        if(showBitmap.getHeight()<getHeight()&&showBitmap.getWidth()<getWidth()){
            //如果图片宽高均小于屏幕宽高，只需要计算图片位移到中心的距离
            initScale=1;
            initTranslateX=(getWidth()-showBitmap.getWidth())/2;
            initTranslateY=(getHeight()-showBitmap.getHeight())/2;

        }else{
            //如果图片宽(高)大于屏幕宽(高)，需要计算图片缩小倍数和位移到中心的距离
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


        circleRectFMatrix =new Matrix();

        //图片未缩放的矩阵
        showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
        showBitmapMatrix =new Matrix();
        showBitmapMatrix.postScale(initScale,initScale);
        showBitmapMatrix.postTranslate(initTranslateX,initTranslateY);

        //图片缩放之后的矩阵
        showBitmapMatrix.mapRect(showBitmapRectF);

        //根据图片矩阵获取圆形矩阵
        circleRectF=getCircleRectFByBitmapRectF(showBitmapRectF);
//        circlePath.addCircle(centerX, centerY,circleRectFLength/2, Path.Direction.CW);
        //记录初始化的圆形矩阵
        initCircleRectF=circleRectF;

        refreshPath();

    }


    private void refreshPaint() {
        circleBorderPaint.setColor(borderColor);
        bgPaint.setColor(maskColor);
    }
    private void refreshPath() {
        if(!outsidePath.isEmpty()){
            outsidePath.reset();
        }
        //圆形之外所有区域
        outsidePath.addRect(new RectF(0,0,getWidth(),getHeight()),Path.Direction.CW);

        if(!circlePath.isEmpty()){
            circlePath.reset();
        }
        if(radius>getRectLength(circleRectF)/2||radius<0){
            radius=getRectLength(circleRectF)/2;
        }
        //圆形之内所有区域
        circlePath.addRoundRect(circleRectF,radius,radius, Path.Direction.CW);

        if(!circleBorderPath.isEmpty()){
            circleBorderPath.reset();
        }
        RectF circleBorderRectF=new RectF(circleRectF.left+ getPathInterval(),circleRectF.top+ getPathInterval(),circleRectF.right- getPathInterval(),circleRectF.bottom- getPathInterval());
        circleBorderPath.addRoundRect(circleBorderRectF,radius*getRectLength(circleBorderRectF)/getRectLength(circleRectF),radius*getRectLength(circleBorderRectF)/getRectLength(circleRectF), Path.Direction.CW);

        //获取圆形之外所有区域
        outsidePath.op(circlePath, Path.Op.XOR);

        this.bigCircleRectF = getBigCircleRectF(circleRectF);

        if(!bigCirclePath.isEmpty()){
            bigCirclePath.reset();
        }
        bigCirclePath.addRoundRect(this.bigCircleRectF,(this.bigCircleRectF.right- this.bigCircleRectF.left)/2,(this.bigCircleRectF.right- this.bigCircleRectF.left)/2, Path.Direction.CW);

        bigCirclePath.op(circlePath,Path.Op.XOR);

        //获取可以触摸放大的区域
        touchRegion.setPath(bigCirclePath,new Region(0,0,getWidth(),getHeight()));
    }

    /*裁剪*/
    public Bitmap clip(){
        if(sizeChanged==false){
            return null;
        }
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);

        Matrix matrix=new Matrix();
        showBitmapMatrix.invert(matrix);

        RectF rectF=new RectF();
        rectF.set(circleRectF);
        matrix.mapRect(rectF);

        Bitmap needCropBitmap = Bitmap.createBitmap(showBitmap, (int) rectF.left, (int) rectF.top, (int) (rectF.right - rectF.left), (int) (rectF.bottom - rectF.top));

        Bitmap newBitmap = Bitmap.createBitmap((int)getRectLength(circleRectF), (int)getRectLength(circleRectF), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(newBitmap);

        int saveCount = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG);

        Path path=new Path();
        path.addRoundRect(new RectF(0,0,getRectLength(circleRectF),getRectLength(circleRectF)),radius,radius, Path.Direction.CW);

        path.moveTo(0,0);
        path.moveTo(getRectLength(circleRectF),getRectLength(circleRectF));

        canvas.drawPath(path,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));


        canvas.drawBitmap(needCropBitmap,
                new Rect(0,0,needCropBitmap.getWidth(),needCropBitmap.getHeight()),
                new RectF(0,0,getRectLength(circleRectF),getRectLength(circleRectF)),paint);
//        canvas.drawBitmap(needCropBitmap,newMatrix,paint);
        paint.setXfermode(null);

        canvas.restoreToCount(saveCount);

        float[]temp=new float[9];
        showBitmapMatrix.getValues(temp);

        if(temp[Matrix.MSCALE_X]<0&&temp[Matrix.MSCALE_Y]<0){
            //如果裁剪时有翻转图片，则对图片做处理
            newBitmap=getFlipBitmap(newBitmap,-1,-1);
        }else if(temp[Matrix.MSCALE_X]<0){
            newBitmap=getFlipBitmap(newBitmap,-1,1);
        }else if(temp[Matrix.MSCALE_Y]<0){
            newBitmap=getFlipBitmap(newBitmap,1,-1);
        }
        needCropBitmap.recycle();
        needCropBitmap=null;
        return newBitmap;
    }
    private Bitmap getFlipBitmap(Bitmap newBitmap,int MSCALE_X,int MSCALE_Y){
        Matrix flipMatrix = new Matrix();
        flipMatrix.postScale(MSCALE_X,MSCALE_Y,newBitmap.getWidth()/2,newBitmap.getWidth()/2);
        newBitmap=Bitmap.createBitmap(newBitmap,0,0,newBitmap.getWidth(),newBitmap.getHeight(),flipMatrix,true);
        return newBitmap;
    }
    /*水平翻转*/
    public void horizontalFlip(){
        post(new Runnable() {
            @Override
            public void run() {
                showBitmapMatrix.postScale(-1,1,centerX,centerY);
                showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
                showBitmapMatrix.mapRect(showBitmapRectF);
                invalidate();
            }
        });
    }
    /**垂直翻转*/
    public void verticalFlip(){
        post(new Runnable() {
            @Override
            public void run() {
                showBitmapMatrix.postScale(1,-1,centerX,centerY);
                showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
                showBitmapMatrix.mapRect(showBitmapRectF);
                invalidate();
            }
        });

    }

    /**垂直+水平翻转*/
    public void verticalAndHorizontalFlip(){
        post(new Runnable() {
            @Override
            public void run() {
                showBitmapMatrix.postScale(-1,-1,centerX,centerY);
                showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
                showBitmapMatrix.mapRect(showBitmapRectF);

                invalidate();
            }
        });
    }
    /*还原*/
    public void reset(){
        init();
        invalidate();
    }
    private float getPathInterval(){
        return dip2px(getContext(),0.5f);
    }
    //根据图片缩放之后的矩阵获取圆形裁剪框的矩阵
    private RectF getCircleRectFByBitmapRectF(RectF showBitmapRectF){
        float rectFW = showBitmapRectF.right - showBitmapRectF.left;
        float rectFH = showBitmapRectF.bottom - showBitmapRectF.top;
        //圆形所在矩阵边长
        float circleRectFLength=rectFW>rectFH?rectFH:rectFW;
        //计算出圆形所在矩阵的left top
        float circleRectFLeft= centerX -circleRectFLength/2;
        float circleRectFTop= centerY -circleRectFLength/2;
        //圆形矩阵
        return new RectF(circleRectFLeft,circleRectFTop,circleRectFLength+circleRectFLeft,circleRectFLength+circleRectFTop);
    }
    private float getRectLength(RectF rectF){
        return Math.abs(rectF.right-rectF.left);
    }
    private RectF getBigCircleRectF(RectF circleRectF){
        RectF rectF =new RectF();
        rectF.set(circleRectF);
        rectF.left= rectF.left-getTouchAreaWidth();
        rectF.top= rectF.top-getTouchAreaWidth();
        rectF.right= rectF.right+getTouchAreaWidth();
        rectF.bottom= rectF.bottom+getTouchAreaWidth();
        return rectF;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(showBitmap==null){
            return;
        }
        canvas.drawBitmap(showBitmap, showBitmapMatrix,null);


        //圆形所在矩阵
//        canvas.drawRect(circleRectF,paint);
//        canvas.drawRect(bigCircleRectF,showBitmapPaint);
        //包含图片矩形
//        canvas.drawRect(showBitmapRectF,showBitmapPaint);//
        canvas.drawPath(outsidePath,bgPaint);

        canvas.drawPath(circleBorderPath,circleBorderPaint);

        //bigpath
//        canvas.drawPath(bigCirclePath,bgPaint);

    }

    private int dip2px(Context context,float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5F);
    }
    private int getTouchAreaWidth(){
        return dip2px(getContext(), 10);
    }

    private float getCurrentScale(){
        float[]temp=new float[9];
        showBitmapMatrix.getValues(temp);
        //加了水平翻转功能进去，所以这里取绝对值
        return Math.abs(temp[Matrix.MSCALE_X]);
    }
    private void initGesture() {
        gestureDetector=new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //通过移动来缩放圆形裁剪框
                if(canZoomCircle){
                    //&&getCurrentScale()<maxScale&&getCurrentScale()>minCircleScale
                    float distance=Math.abs(distanceX)>Math.abs(distanceY)?distanceX:distanceY;
                    float rectHeight = circleRectF.bottom - circleRectF.top;
                    float scaleFactory = (-distance*2 + rectHeight) / rectHeight;

                    //圆形裁剪框和bitmap同时缩放处理
//                    circleRectFMatrix.postScale(scaleFactory,scaleFactory,centerX,centerY);

                    showBitmapMatrix.postScale(scaleFactory,scaleFactory,centerX,centerY);

                    showBitmapRectF = new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
                    showBitmapMatrix.mapRect(showBitmapRectF);


                    circleRectF = new RectF();
                    circleRectF.set(getCircleRectFByBitmapRectF(showBitmapRectF));
//                    circleRectFMatrix.mapRect(circleRectF);

                    bigCircleRectF=getBigCircleRectF(circleRectF);

                    refreshPath();


                    invalidate();
                }
                //移动图片
                if(canMoveBitmap&&canZoomCircle==false){
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
//                                Log(initScale+"==="+initScale);
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
        if(scaleFactor>1&&(getCurrentScale()*scaleFactor)>maxScale){
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
                }
               /* if(touchRegion.contains((int)event.getX(),(int)event.getY())){
                    canZoomCircle=true;
                }else{
                }*/
                break;
            case MotionEvent.ACTION_UP:
                canMoveBitmap=false;
                canZoomCircle=false;
                break;
        }
        return true;
    }

/*    private void Log(String s) {
        Log.i("@@@===","==="+s);
    }*/
    private void resetBitmap(){

        reset();
    }
    public Bitmap getBitmap() {
        return showBitmap;
    }

    @Deprecated
    public LikeQQCropView setBitmap(Bitmap bitmap){
        showBitmap=bitmap;
        resetBitmap();
        return this;
    }
    /*******************************************************************************************************/
    public LikeQQCropView setBitmap(int resId, int reqWidth, int reqHeight) {
        showBitmap= CropViewUtils.compressBitmap(getContext(),resId,reqWidth,reqHeight);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmap(String pathName, int reqWidth, int reqHeight) {
        showBitmap= CropViewUtils.compressBitmap(pathName,reqWidth,reqHeight);
        resetBitmap();
        return this;
    }
    @Deprecated
    public LikeQQCropView setBitmapToRotate(String pathName, int reqWidth, int reqHeight) {
        //内部已做旋转处理
        showBitmap= CropViewUtils.compressBitmap(pathName,reqWidth,reqHeight);
        resetBitmap();
//        int degree = CropViewUtils.readPictureDegree(pathName);
//        if(degree>0){
//            showBitmap=rotateBitmap(degree,showBitmap);
//        }
        return this;
    }
    public LikeQQCropView setBitmap(byte[] data, int offset, int length, int reqWidth, int reqHeight) {
        showBitmap= CropViewUtils.compressBitmap(data,offset,length,reqWidth,reqHeight);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmap(FileDescriptor fd, Rect outPadding, int reqWidth, int reqHeight) {
        showBitmap= CropViewUtils.compressBitmap(fd,outPadding,reqWidth,reqHeight);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmap(Resources res, TypedValue value, InputStream is, Rect pad, int reqWidth, int reqHeight) {
        showBitmap= CropViewUtils.compressBitmap(res,value,is,pad,reqWidth,reqHeight);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmap(InputStream is, Rect outPadding, int reqWidth, int reqHeight) {
        showBitmap= CropViewUtils.compressBitmap(is,outPadding,reqWidth,reqHeight);
        resetBitmap();
        return this;
    }
    /*******************************************************************************************************/


    /*******************************************************************************************************/
    public LikeQQCropView setBitmapForHeight(int resId,int reqHeight) {
        showBitmap= CropViewUtils.compressBitmapForHeight(getContext(),resId,reqHeight);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForHeight(String pathName,int reqHeight) {
        showBitmap= CropViewUtils.compressBitmapForHeight(pathName,reqHeight);
        resetBitmap();
        return this;
    }
    @Deprecated
    public LikeQQCropView setBitmapForHeightToRotate(String pathName,int reqHeight) {
        //内部已做旋转处理
        showBitmap= CropViewUtils.compressBitmapForHeight(pathName,reqHeight);
        resetBitmap();
//        int degree = CropViewUtils.readPictureDegree(pathName);
//        if(degree>0){
//            showBitmap=rotateBitmap(degree,showBitmap);
//        }
        return this;
    }
    public LikeQQCropView setBitmapForHeight(byte[] data, int offset, int length,int reqHeight) {
        showBitmap= CropViewUtils.compressBitmapForHeight(data,offset,length,reqHeight);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForHeight(FileDescriptor fd, Rect outPadding,int reqHeight) {
        showBitmap= CropViewUtils.compressBitmapForHeight(fd,outPadding,reqHeight);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForHeight(Resources res, TypedValue value,InputStream is, Rect pad,int reqHeight) {
        showBitmap= CropViewUtils.compressBitmapForHeight(res,value,is,pad,reqHeight);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForHeight(InputStream is, Rect outPadding,int reqHeight) {
        showBitmap= CropViewUtils.compressBitmapForHeight(is,outPadding,reqHeight);
        resetBitmap();
        return this;
    }
    /*******************************************************************************************************/


    /*******************************************************************************************************/
    public LikeQQCropView setBitmapForWidth( int resId, int reqWidth) {
        showBitmap= CropViewUtils.compressBitmapForWidth(getContext(),resId,reqWidth);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForWidth(String pathName, int reqWidth) {
        showBitmap= CropViewUtils.compressBitmapForWidth(pathName,reqWidth);
        resetBitmap();
        return this;
    }
    @Deprecated
    public LikeQQCropView setBitmapForWidthToRotate(String pathName,int reqWidth) {
        //内部已做旋转处理
        showBitmap= CropViewUtils.compressBitmapForWidth(pathName,reqWidth);
        resetBitmap();
//        int degree = CropViewUtils.readPictureDegree(pathName);
//        if(degree>0){
//            showBitmap=rotateBitmap(degree,showBitmap);
//        }
        return this;
    }
    public LikeQQCropView setBitmapForWidth(byte[] data, int offset, int length, int reqWidth) {
        showBitmap= CropViewUtils.compressBitmapForWidth(data,offset,length,reqWidth);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForWidth(FileDescriptor fd, Rect outPadding, int reqWidth) {
        showBitmap= CropViewUtils.compressBitmapForWidth(fd,outPadding,reqWidth);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForWidth(Resources res, TypedValue value,InputStream is, Rect pad, int reqWidth) {
        showBitmap= CropViewUtils.compressBitmapForWidth(res,value,is,pad,reqWidth);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForWidth(InputStream is, Rect outPadding, int reqWidth) {
        showBitmap= CropViewUtils.compressBitmapForWidth(is,outPadding,reqWidth);
        resetBitmap();
        return this;
    }
    /*******************************************************************************************************/


    /*******************************************************************************************************/
    public LikeQQCropView setBitmapForScale(int resId,int scaleSize) {
        showBitmap= CropViewUtils.compressBitmapForScale(getContext(),resId,scaleSize);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForScale(String pathName,int scaleSize) {
        showBitmap= CropViewUtils.compressBitmapForScale(pathName,scaleSize);
        resetBitmap();
        return this;
    }
    @Deprecated
    public LikeQQCropView setBitmapForScaleToRotate(String pathName,int scaleSize) {
        //内部已做旋转处理
        showBitmap= CropViewUtils.compressBitmapForScale(pathName,scaleSize);
        resetBitmap();
//        int degree = CropViewUtils.readPictureDegree(pathName);
//        if(degree>0){
//            showBitmap=rotateBitmap(degree,showBitmap);
//        }
        return this;
    }
    public LikeQQCropView setBitmapForScale(byte[] data, int offset, int length,int scaleSize) {
        showBitmap= CropViewUtils.compressBitmapForScale(data,offset,length,scaleSize);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForScale(FileDescriptor fd, Rect outPadding,int scaleSize) {
        showBitmap= CropViewUtils.compressBitmapForScale(fd,outPadding,scaleSize);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForScale(Resources res, TypedValue value,InputStream is, Rect pad,int scaleSize) {
        showBitmap= CropViewUtils.compressBitmapForScale(res,value,is,pad,scaleSize);
        resetBitmap();
        return this;
    }
    public LikeQQCropView setBitmapForScale(InputStream is, Rect outPadding,int scaleSize) {
        showBitmap= CropViewUtils.compressBitmapForScale(is,outPadding,scaleSize);
        resetBitmap();
        return this;
    }
    /*******************************************************************************************************/


    /**************************************************旋转图片*****************************************************/


    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return
     */
    public Bitmap rotateBitmap(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (resizedBitmap != bitmap && bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return resizedBitmap;
    }
}
