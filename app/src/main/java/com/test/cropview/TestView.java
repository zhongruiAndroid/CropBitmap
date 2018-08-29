package com.test.cropview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static android.content.ContentValues.TAG;

public class TestView extends View {
    public TestView(Context context) {
        super(context);
        init(null);
    }


    public TestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    Paint mDeafultPaint;
    private void init(AttributeSet attrs) {
        mDeafultPaint=new Paint();
        mDeafultPaint.setAntiAlias(true);
        mDeafultPaint.setStyle(Paint.Style.FILL);
        mDeafultPaint.setColor(ContextCompat.getColor(getContext(),R.color.colorAccent));
    }
    public void Log(String str){
        Log.i(TAG+"===","==="+str);
    }
    boolean hasSecondPoint;
    PointF pointF=new PointF();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if(event.getPointerId(actionIndex)==1){
                    hasSecondPoint=true;
                    pointF.set(event.getX(actionIndex),event.getY(actionIndex));
                }
            break;
            case MotionEvent.ACTION_POINTER_UP:
                if(event.getPointerId(actionIndex)==1){
                    hasSecondPoint=false;
                }
            break;
            case MotionEvent.ACTION_UP:
                hasSecondPoint=false;
                if(event.getPointerId(actionIndex)==1){
//                    canDrag=false;
                }
            break;
            case MotionEvent.ACTION_MOVE:
                if(hasSecondPoint){
                    int pointerIndex = event.findPointerIndex(1);
                    pointF.set(event.getX(pointerIndex),event.getY(pointerIndex));
                }
            break;
        }
        Log.i(TAG+"===","==="+super.onTouchEvent(event));
        invalidate();
        return true;
//        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(hasSecondPoint){
            canvas.drawCircle(pointF.x,pointF.y,100,mDeafultPaint);
        }
    }
}
