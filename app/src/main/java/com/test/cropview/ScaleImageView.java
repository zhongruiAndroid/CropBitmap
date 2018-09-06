package com.test.cropview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class ScaleImageView extends View {
    public ScaleImageView(Context context) {
        super(context);
        init();
    }

    public ScaleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScaleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    Paint paint;
    Matrix matrix;
    Bitmap bitmap;
    float initScale,initTranslateX,initTranslateY;
    float maxScale=3f;
    float minScale=0.5f;
    ScaleGestureDetector scaleGestureDetector;
    public void init(){
        bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.bird);
        matrix=new Matrix();
        paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        scaleGestureDetector=new ScaleGestureDetector(getContext(),new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                Log.i("===",detector.getFocusX()+"==="+detector.getFocusY());
                float scaleFactor = detector.getScaleFactor();
                float currentScale=1;
                float[]matrixValues=new float[9];
                matrix.getValues(matrixValues);
                float matrixValue = matrixValues[Matrix.MSCALE_X];
                float reallyScaleFactor=scaleFactor*matrixValue;
                if(scaleFactor>1&&reallyScaleFactor>maxScale){
                    currentScale=maxScale/matrixValue;
                }else if(scaleFactor<1&&reallyScaleFactor<minScale){
                    currentScale=minScale/matrixValue;
                }else{
                    currentScale=scaleFactor;
                }
                matrix.preScale(currentScale,currentScale,detector.getFocusX(),detector.getFocusY());
                invalidate();
                return true;
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(bitmap.getWidth()*1.0f/bitmap.getHeight()>w*1.0f/h){
            initScale=w*1.0f/bitmap.getWidth();
            initTranslateX=0;
            initTranslateY=(h-bitmap.getHeight()*initScale)/2;
        }else{
            initScale=h*1.0f/bitmap.getHeight();
            initTranslateX=(w-bitmap.getWidth()*initScale)/2;
            initTranslateY=0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(initTranslateX,initTranslateY);
        canvas.scale(initScale,initScale,0,0);

        canvas.save();
        canvas.concat(matrix);

        canvas.drawBitmap(bitmap,0,0,paint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
//                Log.i("===",event.getX()+"==="+event.getY());
            break;
        }
        return true;
    }
}
