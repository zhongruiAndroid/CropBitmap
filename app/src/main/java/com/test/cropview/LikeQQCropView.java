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
import android.view.View;

import com.test.cropview.tool.PhoneUtils;

public class LikeQQCropView extends View {
    public LikeQQCropView(Context context) {
        super(context);
    }

    public LikeQQCropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LikeQQCropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    //view显示的图片
    private Bitmap showBitmap;
    //控制图片绘制的矩阵
    private Matrix imageMatrix;
    //初始化图片缩放和平移，保证图片在view中心显示
    private float initScale=1;
    private float initTranslateX;
    private float initTranslateY;


    //圆形所在矩阵
    private RectF circleRectF;
    //通过path在view中显示出圆形
    private Path circlePath;

    //圆形之外的path
    private Path outsidePath;

    private Paint paint;
    private Paint bgPaint;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bgPaint=new Paint(Paint.ANTI_ALIAS_FLAG);

        bgPaint.setColor(ContextCompat.getColor(getContext(),R.color.transparent_half));

        init();
    }
    public void setShowBitmap(Bitmap bitmap){
        showBitmap=bitmap;
    }
    public void init() {
        if(showBitmap==null){
            return;
        }
        paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ContextCompat.getColor(getContext(),R.color.colorAccent));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

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
        RectF rectF=new RectF(0,0,showBitmap.getWidth(),showBitmap.getHeight());
        imageMatrix=new Matrix();
        imageMatrix.postScale(initScale,initScale);
        imageMatrix.postTranslate(initTranslateX,initTranslateY);

        //图片缩放之前的矩阵
        imageMatrix.mapRect(rectF);

        float rectFW = rectF.right - rectF.left;
        float rectFH = rectF.bottom - rectF.top;

        //圆形所在矩阵边长
        float circleRectFLength=rectFW>rectFH?rectFH:rectFW;

        float centerX=getWidth()/2;
        float centerY=getHeight()/2;

        //计算出圆形所在矩阵的left top
        float circleRectFLeft=centerX-circleRectFLength/2;
        float circleRectFTop=centerY-circleRectFLength/2;
        //圆形矩阵
        circleRectF=new RectF(circleRectFLeft,circleRectFTop,circleRectFLength+circleRectFLeft,circleRectFLength+circleRectFTop);

//            circlePath.addRect(circleRectF, Path.Direction.CW);
        circlePath.addCircle(centerX,centerY,circleRectFLength/2, Path.Direction.CW);

        outsidePath.addRect(new RectF(0,0,getWidth(),getHeight()),Path.Direction.CW);

        outsidePath.op(circlePath, Path.Op.XOR);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(showBitmap==null){
           return;
        }
        canvas.drawBitmap(showBitmap,imageMatrix,null);

        circleRectF.left=circleRectF.left-getDP();
        circleRectF.top=circleRectF.top-getDP();
        circleRectF.right=circleRectF.right+getDP();
        circleRectF.bottom=circleRectF.bottom+getDP();

        canvas.drawRect(circleRectF,paint);
        canvas.drawPath(outsidePath,bgPaint);

    }

    public int getDP(){
        int i = PhoneUtils.dip2px(getContext(), 10);
        return i;
    }
}
