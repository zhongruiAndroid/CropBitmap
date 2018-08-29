package com.test.cropview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static android.content.ContentValues.TAG;

public class MoveImageView extends View {


    private Matrix matrix;

    public MoveImageView(Context context) {
        super(context);
        init(null);
    }


    public MoveImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MoveImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private Bitmap bitmap;
    Paint mDeafultPaint;
    RectF rectF;
    private void init(AttributeSet attrs) {
        mDeafultPaint=new Paint();
        mDeafultPaint.setAntiAlias(true);
        mDeafultPaint.setStyle(Paint.Style.FILL);
        mDeafultPaint.setColor(ContextCompat.getColor(getContext(),R.color.colorAccent));

        BitmapFactory.Options options=new BitmapFactory.Options();
        options.outHeight=100;
        options.outWidth=100;

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b, options);
        rectF=new RectF(0,0,bitmap.getWidth(),bitmap.getHeight());
        matrix = new Matrix();

    }
    public void Log(String str){
        Log.i(TAG+"===","==="+str);
    }
    boolean canDrag;
    PointF beforPoint=new PointF();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if(rectF.contains(event.getX(),event.getY())){
                    canDrag=true;
                    beforPoint.set(event.getX(),event.getY());
                }else{
                    canDrag=false;
                }
                break;
            case MotionEvent.ACTION_UP:
                    Log.i(TAG+"===","===ACTION_UP");
                    break;
            case MotionEvent.ACTION_POINTER_UP:
                if(event.getPointerId(event.getActionIndex())==0){
                    canDrag=false;
                }
                    break;
            case MotionEvent.ACTION_MOVE:
                int actionIndex = event.getActionIndex();
                if(canDrag){
                    matrix.postTranslate(event.getX()-beforPoint.x,event.getY()-beforPoint.y);
                    beforPoint.set(event.getX(),event.getY());
                    rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    matrix.mapRect(rectF);
                    invalidate();
                }
            break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, matrix, mDeafultPaint);
    }
}
