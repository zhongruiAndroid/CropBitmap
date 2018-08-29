package com.test.cropview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class MoveCircleView extends View {


    public MoveCircleView(Context context) {
        super(context);
        init();
    }

    public MoveCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MoveCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    GestureDetector gestureDetector;
    Paint mPaint;
    RectF rectF;

    //距离view左边距离
    float scaleLeft;
    //距离view顶部距离
    float scaleTop;

    boolean canMove;

    private float speedX;
    private float speedY;
    private boolean reverseX;
    private boolean reverseY;

    //矩阵边长
    float sideLength=180;
    int viewW;
    int viewH;
    Handler handler;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            rectF.left=rectF.left+speedX/30;
            rectF.top=rectF.top+speedY/30;

            speedX=speedX*0.97f;
            speedY=speedY*0.97f;

            if(Math.abs(speedX)<=10){
                speedX=0;
            }
            if(Math.abs(speedY)<=10){
                speedY=0;
            }

            if(isSide()){
                if(reverseX){
                    speedX=-speedX;
                }
                if(reverseY){
                    speedY=-speedY;
                }
            }

            rectF.right=rectF.left+sideLength;
            rectF.bottom=rectF.top+sideLength;
            invalidate();
            if(speedX==0&&speedY==0){
                handler.removeCallbacks(runnable);
            }else{
                handler.postDelayed(runnable,30);
//                handler.post(runnable);
            }
        }
    };
    private void init() {
        handler=new Handler();
        gestureDetector= new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(canMove){
                    speedX =velocityX;
                    speedY =velocityY;

                    handler.removeCallbacks(runnable);
                    handler.post(runnable);
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(ContextCompat.getColor(getContext(),R.color.blue_point));
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float X=(w-sideLength)/2;
        float Y=(h-sideLength)/2;
        rectF=new RectF(X,Y,X+sideLength,Y+sideLength);
        viewW=w;
        viewH=h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawOval(rectF,mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(contains(event.getX(),event.getY())){
                    speedX=0;
                    speedY=0;
                    canMove=true;
                    scaleLeft=event.getX()-rectF.left;
                    scaleTop=event.getY()-rectF.top;
                }else{
                    canMove=false;
                }
            break;
            case MotionEvent.ACTION_MOVE:
                if(canMove){
                    rectF.left=event.getX()-scaleLeft;
                    rectF.top=event.getY()-scaleTop;

                    if(isSide()){
                        scaleLeft=event.getX()-rectF.left;
                        scaleTop=event.getY()-rectF.top;
                    }
                    rectF.right=rectF.left+sideLength;
                    rectF.bottom=rectF.top+sideLength;
                    invalidate();
                }
            break;
        }
        return true;
    }

    public boolean contains(float x,float y){
        float radius=sideLength/2;
        float centerX=rectF.left+radius;
        float conterY=rectF.top+radius;
        return Math.sqrt(Math.pow((centerX-x),2)+Math.pow((conterY-y),2))<=radius;
    }

    public boolean isSide(){
        boolean flag=false;
        reverseX=false;
        reverseY=false;
        if(rectF.left<0){
            rectF.left=0;
            flag=true;
            reverseX=true;
        }
        if(rectF.top<0){
            rectF.top=0;
            flag=true;
            reverseY=true;
        }
        if(rectF.left+sideLength>viewW){
            rectF.left=viewW-sideLength;
            flag=true;
            reverseX=true;
        }
        if(rectF.top+sideLength>viewH){
            rectF.top=viewH-sideLength;
            flag=true;
            reverseY=true;
        }
        return flag;
    }
}
