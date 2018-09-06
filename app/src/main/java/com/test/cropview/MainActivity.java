package com.test.cropview;

import android.content.Intent;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

public class MainActivity extends AppCompatActivity {
    GestureView gestureView;
    private GestureDetector gestureDetector;
    private View mcv;
    private ScaleImageView siv;
    private String TAG="==";
    private ScaleGestureDetector scaleGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mcv = findViewById(R.id.mcv);
        mcv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a();
            }
        });

        siv = findViewById(R.id.siv);
        initView();
    }
    public void test(View view){
        startActivity(new Intent(this,TestActivity.class));
    }
    public void a( ){
        Log.i("==","=aa==");
        View v= LayoutInflater.from(this).inflate(R.layout.a,null);
        PopupWindow popupWindow=new PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT,false);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new PaintDrawable());
        popupWindow.showAtLocation(mcv,Gravity.CENTER,0,0);
    }
    private void initView() {
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
//                Log.i("===","===onScale");
                Log.i(TAG +"===",detector.getScaleFactor()+"==="+detector.getFocusX()+"==="+detector.getFocusY()+"=="+detector.getPreviousSpan()+"=="+detector.getCurrentSpan());
                return false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
//                Log.i(TAG +"===",detector.getScaleFactor()+"==="+detector.getFocusX()+"==="+detector.getFocusY()+"=="+detector.getPreviousSpan()+"=="+detector.getCurrentSpan());
                return true;
            }
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                Log.i(TAG+"===","===onScaleEnd");
            }
        });
        siv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);
                return true;
            }
        });

        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                Log.i("===","===onDown");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.i("===","===onLongPress====");
                super.onLongPress(e);
            }

            @Override
            public void onShowPress(MotionEvent e) {
                Log.i("===","===onShowPress====");
                super.onShowPress(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.i("===","==="+distanceX+"===="+distanceY);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.i("===","===onFling"+velocityX+"=="+velocityY);
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        } );

        gestureView=findViewById(R.id.view);


        gestureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

    }
}
