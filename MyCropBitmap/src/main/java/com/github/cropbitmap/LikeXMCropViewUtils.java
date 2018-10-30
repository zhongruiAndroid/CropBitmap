package com.github.cropbitmap;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

/***
 *   created by zhongrui on 2018/10/24
 */
public class LikeXMCropViewUtils {


    /*图片移动到view中心需要*/
    public float needMoveX,needMoveY;
    public float initScale=1;

    /*裁剪框内部距离*/
    public float borderDistance;


    /*view中心坐标*/
    public int centerX,centerY;
    /*图片*/
    public Bitmap showBitmap;
    public Matrix showBitmapMatrix;
    public RectF showBitmapRect;
    private RectF initialShowBitmapRect;

    /*裁剪框*/
    public Paint cropPaint;
    public Path cropPath;
    public RectF cropRect;
    public Matrix cropMatrix;
    public int cropColor;
    public float cropWidth=2;

    /*裁剪框遮罩层*/
    public Paint maskLayerPaint;
    public Path  maskLayerPath;
    public Matrix maskMatrix;
    public int maskLayerColor;

    /*裁剪框的四个角*/
    public Paint borderAnglePaint;
    public Path  borderAnglePath;

    /*裁剪框触摸区域*/
    public Paint borderTouchPaint;

    /*裁剪框里面的线*/
    public Paint linePaint;
    public Path linePath;

    /*view宽高*/
    public float viewWidth;
    public float viewHeight;

    public float touchWidth=30;

    public RectF leftBorderTouchRect;
    public RectF topBorderTouchRect;
    public RectF rightBorderTouchRect;
    public RectF bottomBorderTouchRect;

    public RectF leftTopTouchRect;
    public RectF rightTopTouchRect;
    public RectF leftBottomTouchRect;
    public RectF rightBottomTouchRect;

    /*触摸点移动超出裁剪框，然后再移回来，需要这个偏移量计算哪个位置开始移动裁剪框*/
    public float touchOffsetX;
    public float touchOffsetY;

    public void prepare(){
        //图片所在矩阵
        showBitmapMatrix=new Matrix();
        //图片所在矩形
        showBitmapRect=new RectF();
        /*裁剪框*/
        cropMatrix=new Matrix();
        cropPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        cropPaint.setColor(Color.BLUE);
        cropPaint.setStrokeWidth(2);
        cropPaint.setStyle(Paint.Style.STROKE);

        cropRect=new RectF();
        cropPath=new Path();

        /*裁剪框遮罩层*/
        maskMatrix=new Matrix();
        maskLayerPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        maskLayerPaint.setColor(Color.parseColor("#30000000"));
        maskLayerPaint.setStyle(Paint.Style.FILL);


        maskLayerPath=new Path();

        /*裁剪框的四个角*/
        borderAnglePaint=new Paint(Paint.ANTI_ALIAS_FLAG);


        borderAnglePath=new Path();

        /*裁剪框触摸区域*/
        borderTouchPaint=new Paint(Paint.ANTI_ALIAS_FLAG);


        /*裁剪框里面的线*/
        linePaint=new Paint(Paint.ANTI_ALIAS_FLAG);


        linePath=new Path();

        int width = showBitmap.getWidth();
        int height = showBitmap.getHeight();

        leftBorderTouchRect =new RectF();
        topBorderTouchRect =new RectF();
        rightBorderTouchRect =new RectF();
        bottomBorderTouchRect =new RectF();

        leftTopTouchRect =new RectF();
        rightTopTouchRect =new RectF();
        leftBottomTouchRect =new RectF();
        rightBottomTouchRect =new RectF();

        /*显示图片区域path*/
//        Path showBitmapPath=new Path();
//        showBitmapPath.addRect();
        initPath();
        refreshTouchBorder(cropRect);
    }

    public RectF getBitmapRect() {
        if(showBitmapRect!=null){
            showBitmapRect.setEmpty();
        }else{
            showBitmapRect=new RectF();
        }
        showBitmapRect.set(0,0,showBitmap.getWidth(),showBitmap.getHeight());

        showBitmapMatrix.mapRect(showBitmapRect);
        return showBitmapRect;
    }
    public void refreshTouchBorder(RectF cropRect) {
        leftBorderTouchRect.set(cropRect.left,cropRect.top+touchWidth,cropRect.left+touchWidth,cropRect.bottom-touchWidth);
        topBorderTouchRect.set(cropRect.left+touchWidth,cropRect.top,cropRect.right-touchWidth,cropRect.top+touchWidth);
        rightBorderTouchRect.set(cropRect.right-touchWidth,cropRect.top+touchWidth,cropRect.right,cropRect.bottom-touchWidth);
        bottomBorderTouchRect.set(cropRect.left+touchWidth,cropRect.bottom-touchWidth,cropRect.right-touchWidth,cropRect.bottom);

        leftTopTouchRect.set(cropRect.left,cropRect.top,cropRect.left+touchWidth,cropRect.top+touchWidth);
        rightTopTouchRect.set(cropRect.right-touchWidth,cropRect.top,cropRect.right,cropRect.top+touchWidth);
        leftBottomTouchRect.set(cropRect.left,cropRect.bottom-touchWidth,cropRect.left+touchWidth,cropRect.bottom);
        rightBottomTouchRect.set(cropRect.right-touchWidth,cropRect.bottom-touchWidth,cropRect.right,cropRect.bottom);
    }
    private void Log(String str){
        Log.i("===","@@==="+str);
    };
    public void initPath(){
        //裁剪框
        if(!cropPath.isEmpty()){
            cropPath.reset();
        }
        cropRect.set(needMoveX,needMoveY,viewWidth-1f*needMoveX,viewHeight-1f*needMoveY);
        Log(cropRect.toString());///
        cropPath.addRect(cropRect, Path.Direction.CW);

        //遮罩层
        if(!maskLayerPath.isEmpty()){
            maskLayerPath.reset();
        }
        maskLayerPath.addRect(new RectF(0,0,viewWidth,viewHeight), Path.Direction.CW);
        maskLayerPath.op(cropPath, Path.Op.XOR);

    }
    public void refreshCropPath() {
        if(!cropPath.isEmpty()){
            cropPath.reset();
        }
        cropPath.addRect(cropRect, Path.Direction.CW);

        //遮罩层
        if(!maskLayerPath.isEmpty()){
            maskLayerPath.reset();
        }
        maskLayerPath.addRect(new RectF(0,0,viewWidth,viewHeight), Path.Direction.CW);
        maskLayerPath.op(cropPath, Path.Op.XOR);
    }
    private void refreshPaint() {

    }
    public void refreshShowBitmapRect(){
        showBitmapRect.set(0,0,showBitmap.getWidth(),showBitmap.getHeight());
        showBitmapMatrix.mapRect(showBitmapRect);
    }
}
