package com.test.cropview;

import android.content.Context;
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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class CircleView extends View {

    private GestureDetector gestureDetector;
    private Path a;

    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    Paint paint;
    Matrix matrix;
    RectF rect;
    Path path;

    RectF bigRect;
    Path bigPath;
    Region touchRegion;

    private void init() {
        paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(getContext(),R.color.colorAccent));


        rect=new RectF(0,0,200,200);
        bigRect=new RectF(-40,-40,240,240);

        matrix=new Matrix();
        matrix.postTranslate(400,400);
        matrix.mapRect(rect);
        matrix.mapRect(bigRect);

        path=new Path();
        path.addOval(rect, Path.Direction.CW);

        bigPath=new Path();
        bigPath.addOval(bigRect, Path.Direction.CW);

        a = new Path();
        bigPath.op(path, bigPath, Path.Op.XOR);

        touchRegion=new Region();
        Region globalRegion = new Region(0,0,getWidth(),getHeight());
        touchRegion.setPath(bigPath,globalRegion);

        gestureDetector = new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        gestureDetector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(touchRegion.contains((int)event.getX(),(int)event.getY())){
                    Log(event.getX()+"=contains==="+event.getY());
                }else{
                    Log(event.getX()+"==="+event.getY());
                }
            break;
        }
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawOval(rect,paint);
        canvas.drawPath(bigPath,paint);
//        canvas.drawPath(path,paint);

        /*RegionIterator iterator = new RegionIterator(touchRegion);
        Rect rect = new Rect();
        while (iterator.next(rect)) {
            canvas.drawRect(rect, paint);
        }*/
    }

    private void Log(String s) {
        Log.i("@@@===","==="+s);
    }
}
