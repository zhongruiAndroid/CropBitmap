package com.github.cropbitmap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.util.TypedValue;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

public class CropViewUtils {

    /*******************************************************************************************************/
    /**
     * 对图片进行压缩，主要是为了解决控件显示过大图片占用内存造成OOM问题,一般压缩后的图片大小应该和用来展示它的控件大小相近.
     *
     * @param context   上下文
     * @param resId     图片资源Id
     * @param reqWidth  期望压缩的宽度
     * @param reqHeight 期望压缩的高度
     * @return 压缩后的图片
     */
    public static Bitmap compressBitmap(Context context, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = Math.max(heightRatio, widthRatio);
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmap(BitmapFactory.decodeResource(context.getResources(), resId, options), reqWidth, reqHeight);
    }

    public static Bitmap compressBitmap(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        int degree = readPictureDegree(pathName);
        if(is90And270(degree)){
            //如果图片出现旋转，则调换宽高换算缩放率
            reqWidth=reqHeight-reqWidth;
            reqHeight=reqHeight-reqWidth;
            reqWidth=reqHeight+reqWidth;
        }
        if (height > reqHeight || width > reqWidth) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = Math.max(heightRatio, widthRatio);
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmap(BitmapFactory.decodeFile(pathName, options), reqWidth, reqHeight,degree);
    }

    public static Bitmap compressBitmap(byte[] data, int offset, int length, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, offset, length, options);

        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = Math.max(heightRatio, widthRatio);
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmap(BitmapFactory.decodeByteArray(data, offset, length, options), reqWidth, reqHeight);
    }

    public static Bitmap compressBitmap(FileDescriptor fd, Rect outPadding, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, outPadding, options);

        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = Math.max(heightRatio, widthRatio);
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmap(BitmapFactory.decodeFileDescriptor(fd, outPadding, options), reqWidth, reqHeight);
    }

    public static Bitmap compressBitmap(Resources res, TypedValue value, InputStream is, Rect pad, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResourceStream(res, value, is, pad, options);

        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = Math.max(heightRatio, widthRatio);
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmap(BitmapFactory.decodeResourceStream(res, value, is, pad, options), reqWidth, reqHeight);
    }

    public static Bitmap compressBitmap(InputStream is, Rect outPadding, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, outPadding, options);

        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = Math.max(heightRatio, widthRatio);
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmap(BitmapFactory.decodeStream(is, outPadding, options), reqWidth, reqHeight);
    }
    /*******************************************************************************************************/


    /*******************************************************************************************************/
    public static Bitmap compressBitmapForHeight(Context context, int resId, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);

        int height = options.outHeight;
//        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            inSampleSize = heightRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForHeight(BitmapFactory.decodeResource(context.getResources(), resId, options), reqHeight);
    }

    public static Bitmap compressBitmapForHeight(String pathName, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        int height = options.outHeight;
//          int width = options.outWidth;
        int inSampleSize = 1;
        int degree = readPictureDegree(pathName);
        if(is90And270(degree)){
            height=options.outWidth;
        }
        if (height > reqHeight) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            inSampleSize = heightRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForHeight(BitmapFactory.decodeFile(pathName, options), reqHeight,degree);
    }

    public static Bitmap compressBitmapForHeight(byte[] data, int offset, int length, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, offset, length, options);

        int height = options.outHeight;
//          int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            inSampleSize = heightRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForHeight(BitmapFactory.decodeByteArray(data, offset, length, options), reqHeight);
    }

    public static Bitmap compressBitmapForHeight(FileDescriptor fd, Rect outPadding, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, outPadding, options);

        int height = options.outHeight;
//          int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            inSampleSize = heightRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForHeight(BitmapFactory.decodeFileDescriptor(fd, outPadding, options), reqHeight);
    }

    public static Bitmap compressBitmapForHeight(Resources res, TypedValue value, InputStream is, Rect pad, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResourceStream(res, value, is, pad, options);

        int height = options.outHeight;
//          int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            inSampleSize = heightRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForHeight(BitmapFactory.decodeResourceStream(res, value, is, pad, options), reqHeight);
    }

    public static Bitmap compressBitmapForHeight(InputStream is, Rect outPadding, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, outPadding, options);

        int height = options.outHeight;
//          int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight) {
            int heightRatio = (int) Math.floor((float) height / (float) reqHeight);
            inSampleSize = heightRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForHeight(BitmapFactory.decodeStream(is, outPadding, options), reqHeight);
    }
    /*******************************************************************************************************/


    /*******************************************************************************************************/
    public static Bitmap compressBitmapForWidth(Context context, int resId, int reqWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);

//        final int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = widthRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForWidth(BitmapFactory.decodeResource(context.getResources(), resId, options), reqWidth);
    }

    public static Bitmap compressBitmapForWidth(String pathName, int reqWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

//          int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        int degree = readPictureDegree(pathName);
        if(is90And270(degree)){
            width=options.outHeight;
        }
        if (width > reqWidth) {
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = widthRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForWidth(BitmapFactory.decodeFile(pathName, options), reqWidth,degree);
    }

    public static Bitmap compressBitmapForWidth(byte[] data, int offset, int length, int reqWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, offset, length, options);

//          int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = widthRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForWidth(BitmapFactory.decodeByteArray(data, offset, length, options), reqWidth);
    }

    public static Bitmap compressBitmapForWidth(FileDescriptor fd, Rect outPadding, int reqWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, outPadding, options);

//          int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = widthRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForWidth(BitmapFactory.decodeFileDescriptor(fd, outPadding, options), reqWidth);
    }

    public static Bitmap compressBitmapForWidth(Resources res, TypedValue value, InputStream is, Rect pad, int reqWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResourceStream(res, value, is, pad, options);

//          int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = widthRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForWidth(BitmapFactory.decodeResourceStream(res, value, is, pad, options), reqWidth);
    }

    public static Bitmap compressBitmapForWidth(InputStream is, Rect outPadding, int reqWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, outPadding, options);

//          int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            int widthRatio = (int) Math.floor((float) width / (float) reqWidth);
            inSampleSize = widthRatio;
        }
        options.inSampleSize = inSampleSize;
        // 使用计算得到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return scaleBitmapForWidth(BitmapFactory.decodeStream(is, outPadding, options), reqWidth);
    }
    /*******************************************************************************************************/


    /*******************************************************************************************************/
    public static Bitmap compressBitmapForScale(Context context, int resId, int scaleSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        if (scaleSize > 1) {
            options.inSampleSize = scaleSize;
        } else {
            options.inSampleSize = 1;
        }
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }

    public static Bitmap compressBitmapForScale(String pathName, int scaleSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        if (scaleSize > 1) {
            options.inSampleSize = scaleSize;
        } else {
            options.inSampleSize = 1;
        }
        int degree = readPictureDegree(pathName);
        if(is90And270(degree)){
            Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);
            Bitmap newBitmap = createBitmapForMatrix(bitmap,  1, degree);
           /* bitmap.recycle();
            bitmap=null;*/
            return newBitmap;
        }
        return BitmapFactory.decodeFile(pathName, options);
    }

    public static Bitmap compressBitmapForScale(byte[] data, int offset, int length, int scaleSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        if (scaleSize > 1) {
            options.inSampleSize = scaleSize;
        } else {
            options.inSampleSize = 1;
        }
        return BitmapFactory.decodeByteArray(data, offset, length, options);
    }

    public static Bitmap compressBitmapForScale(FileDescriptor fd, Rect outPadding, int scaleSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        if (scaleSize > 1) {
            options.inSampleSize = scaleSize;
        } else {
            options.inSampleSize = 1;
        }
        return BitmapFactory.decodeFileDescriptor(fd, outPadding, options);
    }

    public static Bitmap compressBitmapForScale(Resources res, TypedValue value, InputStream is, Rect pad, int scaleSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        if (scaleSize > 1) {
            options.inSampleSize = scaleSize;
        } else {
            options.inSampleSize = 1;
        }
        return BitmapFactory.decodeResourceStream(res, value, is, pad, options);
    }

    public static Bitmap compressBitmapForScale(InputStream is, Rect outPadding, int scaleSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /*
         * 第一次解析时，inJustDecodeBounds设置为true，
         * 禁止为bitmap分配内存，虽然bitmap返回值为空，但可以获取图片大小
         */
        if (scaleSize > 1) {
            options.inSampleSize = scaleSize;
        } else {
            options.inSampleSize = 1;
        }
        return BitmapFactory.decodeStream(is, outPadding, options);
    }
    /*******************************************************************************************************/


    /*******************************************************************************************************/
    public static Bitmap scaleBitmapForWidth(Bitmap bitmap, int newWidth,int degree) {
        int bWidth=bitmap.getWidth();
        int bHeight=bitmap.getHeight();
        if(is90And270(degree)){
            //如果图片出现旋转，则调换宽高换算缩放率
            bWidth = bitmap.getHeight();
            bHeight = bitmap.getWidth();
        }

        float initScale = newWidth * 1.0f / bWidth;

//        int newHeight = (int) (bHeight * initScale);

        Bitmap newBitmap =  createBitmapForMatrix(bitmap,initScale,degree);

        return newBitmap;
    }
    public static Bitmap scaleBitmapForWidth(Bitmap bitmap, int newWidth) {
        return scaleBitmapForWidth(bitmap,newWidth,0);
//        return Bitmap.createBitmap(bitmap,0,0,newWidth,(int)(bitmap.getHeight()*initScale),matrix,true);
    }

    public static Bitmap scaleBitmapForHeight(Bitmap bitmap, int newHeight,int degree) {
        int bWidth=bitmap.getWidth();
        int bHeight=bitmap.getHeight();
        if(is90And270(degree)){
            //如果图片出现旋转，则调换宽高换算缩放率
            bWidth = bitmap.getHeight();
            bHeight = bitmap.getWidth();
        }
        float initScale = newHeight * 1.0f / bHeight;

//        int newWidth = (int) (bWidth * initScale);

        Bitmap newBitmap = createBitmapForMatrix(bitmap,   initScale, degree);

        return newBitmap;
    }
    public static Bitmap scaleBitmapForHeight(Bitmap bitmap, int newHeight) {
        return scaleBitmapForHeight(bitmap,newHeight,0);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight,int degree) {
        float initScale=1;
        float initTranslateX;
        float initTranslateY;
        if(bitmap.getWidth()*1.0f/bitmap.getHeight()>newWidth*1.0f/newHeight){
            initScale=newWidth*1.0f/bitmap.getWidth();
            initTranslateX=0;
            initTranslateY=(newHeight-bitmap.getHeight()*initScale)/2;
        }else{
            initScale=newHeight*1.0f/bitmap.getHeight();
            initTranslateX=(newWidth-bitmap.getWidth()*initScale)/2;
            initTranslateY=0;
        }
        Matrix matrix=new Matrix();
        matrix.postScale(initScale,initScale);
        matrix.postTranslate(initTranslateX,initTranslateY);
        Bitmap newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(newBitmap);
        canvas.drawBitmap(bitmap,matrix,null);

        matrix.setRotate(degree);
        newBitmap=Bitmap.createBitmap(newBitmap,0,0,newBitmap.getWidth(),newBitmap.getHeight(),matrix,true);
        /*bitmap.recycle();
        bitmap=null;*/
        return newBitmap;
    }
    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        return scaleBitmap(bitmap,newWidth,newHeight,0);
//        return Bitmap.createBitmap(bitmap,0,0,newWidth,newHeight,matrix,true);
    }

    /*******************************************************************************************************/

    public static int[] getBitmapSize(Resources res, int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, id, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    public static int[] getBitmapSize(byte[] data, int offset, int length) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, offset, length, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    public static int[] getBitmapSize(String pathName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    public static int[] getBitmapSize(FileDescriptor fd, Rect outPadding) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, outPadding, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    public static int[] getBitmapSize(Resources res, TypedValue value, InputStream is, Rect pad) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResourceStream(res, value, is, pad, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    public static int[] getBitmapSize(InputStream is, Rect outPadding) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, outPadding, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    /**
     * 读取图片旋转的角度
     *
     * @param filePath
     * @return
     */
    public static int readPictureDegree(String filePath) {
        int rotate = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int result = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (result) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotate;
    }
    private static boolean is90And270(int degree){
        if(degree>0&&(degree==90||degree%270==0)){
            return true;
        }
        return false;
    }

    private static Bitmap createBitmapForMatrix(Bitmap bitmap,float initScale,int degree){
        Matrix matrix=new Matrix();
        matrix.postRotate(degree);
        matrix.postScale(initScale,initScale);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        /*bitmap.recycle();
        bitmap=null;*/
        return newBitmap;
    }
}
