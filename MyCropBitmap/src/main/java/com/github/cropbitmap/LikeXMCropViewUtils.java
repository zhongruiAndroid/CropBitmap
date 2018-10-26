package com.github.cropbitmap;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/***
 *   created by zhongrui on 2018/10/24
 */
public class LikeXMCropViewUtils {
    /*图片*/
    public Bitmap showBitmap;
    public Matrix showBitmapMatrix;

    /*裁剪框*/
    public Matrix cropMatrix;
    public Paint cropPaint;
    public Path cropPath;
    public int cropColor;
    public float cropWidth=2;

    /*裁剪框遮罩层*/
    public Paint maskLayerPaint;
    public Path  maskLayerPath;
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

 /*   public Path leftBorderTouchPath;
    public Path topBorderTouchPath;
    public Path rightBorderTouchPath;
    public Path bottomBorderTouchPath;

    public Path leftTopTouchPath;
    public Path rightTopTouchPath;
    public Path leftBottomTouchPath;
    public Path rightBottomTouchPath;*/

    public void prepare(){

        /*裁剪框*/
        cropMatrix=new Matrix();
        cropPaint=new Paint(Paint.ANTI_ALIAS_FLAG);


        cropPath=new Path();

        /*裁剪框遮罩层*/
        maskLayerPaint=new Paint(Paint.ANTI_ALIAS_FLAG);


        maskLayerPath=new Path();
        maskLayerPath.addRect(new RectF(0,0,viewWidth,viewHeight), Path.Direction.CW);

        /*裁剪框的四个角*/
        borderAnglePaint=new Paint(Paint.ANTI_ALIAS_FLAG);


        borderAnglePath=new Path();

        /*裁剪框触摸区域*/
        borderTouchPaint=new Paint(Paint.ANTI_ALIAS_FLAG);


        /*裁剪框里面的线*/
        linePaint=new Paint(Paint.ANTI_ALIAS_FLAG);


        linePath=new Path();
    }

    public void a(){
    }
}
